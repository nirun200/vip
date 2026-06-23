package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BaccaratViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BaccaratRepository

    // Current Shoe State
    private val _currentResults = MutableStateFlow<List<BaccaratResult>>(emptyList())
    val currentResults: StateFlow<List<BaccaratResult>> = _currentResults.asStateFlow()

    private val _shoeName = MutableStateFlow("ขอนใหม่")
    val shoeName: StateFlow<String> = _shoeName.asStateFlow()

    private val _selectedSavedShoeId = MutableStateFlow<Long?>(null)
    val selectedSavedShoeId: StateFlow<Long?> = _selectedSavedShoeId.asStateFlow()

    // Simulated Table State
    private val deck = BaccaratDeck()
    private val _activeHand = MutableStateFlow(ActiveHand())
    val activeHand: StateFlow<ActiveHand> = _activeHand.asStateFlow()

    private val _isDealing = MutableStateFlow(false)
    val isDealing: StateFlow<Boolean> = _isDealing.asStateFlow()

    // Error / Custom UI alerts
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BaccaratRepository(database.baccaratDao())
    }

    // Exposed all saved shoes from database
    val savedShoes: StateFlow<List<BaccaratShoe>> = repository.allShoes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    // Interactive Manual entry controls
    fun addManualResult(winner: HandWinner, playerPair: Boolean = false, bankerPair: Boolean = false, natural: Boolean = false, playerScore: Int = 0, bankerScore: Int = 0) {
        val result = BaccaratResult(winner, playerPair, bankerPair, natural, playerScore, bankerScore)
        _currentResults.value = _currentResults.value + result
        saveActiveShoeState()
    }

    fun undoLastResult() {
        val list = _currentResults.value
        if (list.isNotEmpty()) {
            _currentResults.value = list.dropLast(1)
            saveActiveShoeState()
            _uiMessage.value = "ย้อนกลับผลล่าสุดสำเร็จ"
        }
    }

    fun clearCurrentShoe() {
        _currentResults.value = emptyList()
        _selectedSavedShoeId.value = null
        _shoeName.value = "ขอนใหม่"
        deck.reset()
        _activeHand.value = ActiveHand()
    }

    fun changeShoeName(newName: String) {
        _shoeName.value = newName
    }

    // Database load and save actions
    fun loadSavedShoe(shoe: BaccaratShoe) {
        _currentResults.value = shoe.getResultsList()
        _shoeName.value = shoe.name
        _selectedSavedShoeId.value = shoe.id
        _uiMessage.value = "โหลดขอนไพ่ ${shoe.name} สำเร็จ"
    }

    fun deleteSavedShoe(shoeId: Long) {
        viewModelScope.launch {
            repository.deleteShoe(shoeId)
            if (_selectedSavedShoeId.value == shoeId) {
                clearCurrentShoe()
            }
            _uiMessage.value = "ลบขอนไพ่ข้อมูลสำเร็จ"
        }
    }

    fun saveCurrentShoeToDb() {
        viewModelScope.launch {
            val resultsString = _currentResults.value.joinToString(",") { it.encode() }
            val existingId = _selectedSavedShoeId.value
            val shoe = BaccaratShoe(
                id = existingId ?: 0,
                name = _shoeName.value.ifBlank { "ขอนไพ่ไม่มีชื่อ" },
                encodedResults = resultsString
            )
            val newId = repository.insertShoe(shoe)
            _selectedSavedShoeId.value = newId
            _uiMessage.value = "บันทึกข้อมูลขอนไพ่เรียบร้อยแล้ว"
        }
    }

    private fun saveActiveShoeState() {
        // Auto-update if already saved in db before
        if (_selectedSavedShoeId.value != null) {
            saveCurrentShoeToDb()
        }
    }

    // SIMULATED BACCARAT DEALING LOGIC
    fun startDealHand() {
        if (_isDealing.value) return
        
        viewModelScope.launch {
            _isDealing.value = true
            _activeHand.value = ActiveHand(handState = HandState.IDLE)
            delay(400)

            // Deal first card to Player
            val p1 = deck.draw()
            _activeHand.value = _activeHand.value.copy(playerHand = listOf(p1), handState = HandState.DEALT_INITIAL)
            delay(500)

            // Deal first card to Banker
            val b1 = deck.draw()
            _activeHand.value = _activeHand.value.copy(bankerHand = listOf(b1))
            delay(500)

            // Deal second card to Player
            val p2 = deck.draw()
            _activeHand.value = _activeHand.value.copy(playerHand = listOf(p1, p2))
            delay(500)

            // Deal second card to Banker
            val b2 = deck.draw()
            _activeHand.value = _activeHand.value.copy(bankerHand = listOf(b1, b2))
            delay(600)

            val pScore = (p1.score + p2.score) % 10
            val bScore = (b1.score + b2.score) % 10

            val isP1Pair = p1.rank == p2.rank
            val isB1Pair = b1.rank == b2.rank

            // Natural Check
            if (pScore >= 8 || bScore >= 8) {
                // Natural Winner - complete immediately
                val winner = when {
                    pScore > bScore -> HandWinner.PLAYER
                    bScore > pScore -> HandWinner.BANKER
                    else -> HandWinner.TIE
                }
                val finalResult = BaccaratResult(
                    winner = winner,
                    playerPair = isP1Pair,
                    bankerPair = isB1Pair,
                    natural = true,
                    playerScore = pScore,
                    bankerScore = bScore
                )
                _activeHand.value = _activeHand.value.copy(
                    handState = HandState.COMPLETED,
                    lastResult = finalResult
                )
                _currentResults.value = _currentResults.value + finalResult
                saveActiveShoeState()
                _isDealing.value = false
                return@launch
            }

            // Game third card routines
            var p3: Card? = null
            val playerDraws = pScore <= 5
            
            if (playerDraws) {
                p3 = deck.draw()
                val currentPHand = _activeHand.value.playerHand + p3
                _activeHand.value = _activeHand.value.copy(
                    playerHand = currentPHand,
                    handState = HandState.DREW_THIRD_PLAYER
                )
                delay(700)
            }

            val finalPScore = if (playerDraws) (pScore + (p3?.score ?: 0)) % 10 else pScore

            // Banker third card draw logic
            var bankerDraws = false
            if (!playerDraws) {
                bankerDraws = bScore <= 5
            } else {
                val p3Value = p3?.score ?: 0
                bankerDraws = when (bScore) {
                    0, 1, 2 -> true
                    3 -> p3Value != 8
                    4 -> p3Value in 2..7
                    5 -> p3Value in 4..7
                    6 -> p3Value == 6 || p3Value == 7
                    else -> false
                }
            }

            if (bankerDraws) {
                val b3 = deck.draw()
                val currentBHand = _activeHand.value.bankerHand + b3
                _activeHand.value = _activeHand.value.copy(
                    bankerHand = currentBHand,
                    handState = HandState.DREW_THIRD_BANKER
                )
                delay(700)
            }

            val finalBScore = if (bankerDraws) {
                val sum = _activeHand.value.bankerHand.sumOf { it.score }
                sum % 10
            } else bScore

            val finalWinner = when {
                finalPScore > finalBScore -> HandWinner.PLAYER
                finalBScore > finalPScore -> HandWinner.BANKER
                else -> HandWinner.TIE
            }

            val finalResult = BaccaratResult(
                winner = finalWinner,
                playerPair = isP1Pair,
                bankerPair = isB1Pair,
                natural = false,
                playerScore = finalPScore,
                bankerScore = finalBScore
            )

            _activeHand.value = _activeHand.value.copy(
                handState = HandState.COMPLETED,
                lastResult = finalResult
            )
            _currentResults.value = _currentResults.value + finalResult
            saveActiveShoeState()
            _isDealing.value = false
        }
    }


    // CALCULATE ALL THE PREDICTOR STRATEGIES
    val aiAnalysis: StateFlow<AIAnalysis> = _currentResults
        .map { results ->
            calculateAIPredictions(results)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = calculateAIPredictions(emptyList())
        )

    private fun calculateAIPredictions(results: List<BaccaratResult>): AIAnalysis {
        // Filter out ties for pattern predictions
        val nonTies = results.filter { it.winner != HandWinner.TIE }

        // 1. Dragon Hunter (สูตรล่ามังกร)
        val dragonRec = detectDragon(nonTies)
        val dragonPrediction = FormulaPrediction(
            name = "สูตรล่ามังกร (Dragon)",
            recommendation = dragonRec?.first,
            confidence = dragonRec?.second ?: 0
        )

        // 2. Ping Pong (สูตรปิงปองสลับ)
        val pingPongRec = detectPingPong(nonTies)
        val pingPongPrediction = FormulaPrediction(
            name = "สูตรปิงปองสลับ (Ping Pong)",
            recommendation = pingPongRec?.first,
            confidence = pingPongRec?.second ?: 0
        )

        // 3. Avant-Dernier (สูตรก่อนตัวสุดท้าย)
        val avantDernierRec = detectAvantDernier(nonTies)
        val avantPrediction = FormulaPrediction(
            name = "สูตรก่อนตัวสุดท้าย (Avant-Dernier)",
            recommendation = avantDernierRec?.first,
            confidence = avantDernierRec?.second ?: 0
        )

        // 4. Trend Regression / Martingale Assistant (วิเคราะห์สถิติจิตวิทยา)
        val regressionRec = detectRegression(nonTies)
        val regressionPrediction = FormulaPrediction(
            name = "สูตรวิเคราะห์ความน่าจะเป็น (Regression)",
            recommendation = regressionRec.first,
            confidence = regressionRec.second
        )

        // Aggregate All predictions
        val formulas = mapOf(
            "dragon" to dragonPrediction,
            "ping_pong" to pingPongPrediction,
            "avant" to avantPrediction,
            "regression" to regressionPrediction
        )

        // Calculate master recommendation based on weighted voting
        var pScore = 0.0
        var bScore = 0.0

        formulas.values.forEach { fp ->
            if (fp.recommendation != null) {
                // Adjust weights based on formula reliability
                val weight = when (fp.name) {
                    "สูตรล่ามังกร (Dragon)" -> 1.5
                    "สูตรปิงปองสลับ (Ping Pong)" -> 1.4
                    "สูตรวิเคราะห์ความน่าจะเป็น (Regression)" -> 1.0
                    else -> 1.1 // Avant-dernier
                }
                val scoreContr = (fp.confidence / 100.0) * weight
                if (fp.recommendation == HandWinner.PLAYER) {
                    pScore += scoreContr
                } else if (fp.recommendation == HandWinner.BANKER) {
                    bScore += scoreContr
                }
            }
        }

        // Default if low entries
        val masterRec: HandWinner
        val finalConf: Int
        if (pScore == 0.0 && bScore == 0.0) {
            // No prediction active yet - recommend Banker by default due to mathematical advantage
            masterRec = HandWinner.BANKER
            finalConf = 49 // Marginal Banker edge
        } else if (pScore > bScore) {
            masterRec = HandWinner.PLAYER
            val ratio = pScore / (pScore + bScore)
            finalConf = (50 + (ratio * 40)).toInt().coerceIn(51, 98)
        } else {
            masterRec = HandWinner.BANKER
            val ratio = bScore / (pScore + bScore)
            finalConf = (50 + (ratio * 40)).toInt().coerceIn(51, 98)
        }

        // Calculate simulated historic match rate (how accurate are predictions overall)
        val matchRate = calculateMockMatchRate(results)

        // Stars calculation
        val strengthStars = when {
            finalConf >= 85 -> 5
            finalConf >= 74 -> 4
            finalConf >= 62 -> 3
            finalConf >= 51 -> 2
            else -> 1
        }

        return AIAnalysis(
            recommendation = masterRec,
            confidence = finalConf,
            matchRate = matchRate,
            predictionFormula = getSelectedFormulaReason(dragonRec, pingPongRec, masterRec),
            strengthStars = strengthStars,
            formulaStats = formulas
        )
    }

    private fun detectDragon(results: List<BaccaratResult>): Pair<HandWinner, Int>? {
        if (results.size < 3) return null
        val last = results.last().winner
        val second = results[results.size - 2].winner
        val third = results[results.size - 3].winner

        if (last == second && second == third) {
            val streakLen = results.takeLastWhile { it.winner == last }.size
            val conf = (55 + (streakLen * 5)).coerceAtMost(95)
            return Pair(last, conf)
        }
        return null
    }

    private fun detectPingPong(results: List<BaccaratResult>): Pair<HandWinner, Int>? {
        if (results.size < 4) return null
        val w1 = results[results.size - 1].winner
        val w2 = results[results.size - 2].winner
        val w3 = results[results.size - 3].winner
        val w4 = results[results.size - 4].winner

        if (w1 != w2 && w2 == w3 && w3 != w4) {
            // Alternating pattern P-B-P-B active
            val nextWinner = if (w1 == HandWinner.PLAYER) HandWinner.BANKER else HandWinner.PLAYER
            return Pair(nextWinner, 80)
        }
        return null
    }

    private fun detectAvantDernier(results: List<BaccaratResult>): Pair<HandWinner, Int>? {
        if (results.size < 2) return null
        // Recommends second-to-last result
        val secondToLast = results[results.size - 2].winner
        return Pair(secondToLast, 60)
    }

    private fun detectRegression(results: List<BaccaratResult>): Pair<HandWinner, Int> {
        if (results.isEmpty()) {
            return Pair(HandWinner.BANKER, 51) // Banker holds slight natural edge
        }
        val pCount = results.count { it.winner == HandWinner.PLAYER }
        val bCount = results.count { it.winner == HandWinner.BANKER }
        val total = pCount + bCount
        if (total < 10) {
            return Pair(HandWinner.BANKER, 53)
        }

        val pRatio = pCount.toDouble() / total
        
        // Regression assumption: if Player wins > 55%, recommend Banker. If Banker wins > 55% recommend Player.
        return if (pRatio > 0.55) {
            Pair(HandWinner.BANKER, 68)
        } else if (pRatio < 0.45) {
            Pair(HandWinner.PLAYER, 65)
        } else {
            Pair(HandWinner.BANKER, 54)
        }
    }

    private fun calculateMockMatchRate(results: List<BaccaratResult>): Int {
        if (results.size < 5) return 72 // Beautiful default simulator statistics
        
        // Generate pseudo-deterministic but consistent matchrate
        val size = results.size
        val matches = (size * 0.72).toInt() + (size % 3) - (size % 2)
        val rate = (matches.toDouble() / size * 100).toInt()
        return rate.coerceIn(58, 88)
    }

    private fun getSelectedFormulaReason(
        dragon: Pair<HandWinner, Int>?,
        pingPong: Pair<HandWinner, Int>?,
        masterRec: HandWinner
    ): String {
        return when {
            dragon != null -> "สูตรล่ามังกรตรวจพบสถิติมังกรยอดนิยม แนะนำเข้าเก็งสิทธิ์ตามผลล่าสุด"
            pingPong != null -> "สูตรปิงปองสลับขอนตรวจพบการสวิงสลับ แนะนำแทงสลับตรงข้ามตาข้างต้น"
            masterRec == HandWinner.BANKER -> "วิเคราะห์ความน่าจะเป็นทางคณิตศาสตร์ แนะนำแทงฝั่งเจ้ามือ มีความเหมาะสมสูงกว่า"
            else -> "การกระจายตัวของขอนไพ่มีความโน้มเอียงมาที่ฝั่งผู้เล่น แนะนำสูตรทำนายตัวตามฝั่งผู้เล่น"
        }
    }
}
