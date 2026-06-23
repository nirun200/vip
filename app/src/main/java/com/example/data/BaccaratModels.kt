package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.random.Random

enum class HandWinner {
    PLAYER, BANKER, TIE
}

data class BaccaratResult(
    val winner: HandWinner,
    val playerPair: Boolean = false,
    val bankerPair: Boolean = false,
    val natural: Boolean = false,
    val playerScore: Int = 0,
    val bankerScore: Int = 0
) {
    // Encodes the result into a small, unbreakable 6-character token:
    // winner(P/B/T), playerPair(1/0), bankerPair(1/0), natural(1/0), playerScore(0-9), bankerScore(0-9)
    fun encode(): String = "${winner.name[0]}${if (playerPair) '1' else '0'}${if (bankerPair) '1' else '0'}${if (natural) '1' else '0'}$playerScore$bankerScore"

    companion object {
        fun decode(str: String): BaccaratResult {
            if (str.length < 6) return BaccaratResult(HandWinner.PLAYER)
            val winner = when (str[0]) {
                'B' -> HandWinner.BANKER
                'T' -> HandWinner.TIE
                else -> HandWinner.PLAYER
            }
            val playerPair = str[1] == '1'
            val bankerPair = str[2] == '1'
            val natural = str[3] == '1'
            val playerScore = str[4].digitToIntOrNull() ?: 0
            val bankerScore = str[5].digitToIntOrNull() ?: 0
            return BaccaratResult(winner, playerPair, bankerPair, natural, playerScore, bankerScore)
        }
    }
}

@Entity(tableName = "baccarat_shoes")
data class BaccaratShoe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val encodedResults: String = "", // Comma-separated BaccaratResult tokens
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getResultsList(): List<BaccaratResult> {
        if (encodedResults.isBlank()) return emptyList()
        return encodedResults.split(",").map { BaccaratResult.decode(it) }
    }
}

// Baccarat Casino Card Deck and Dealing Engine
enum class CardSuit {
    HEART, DIAMOND, CLUB, SPADE;
    
    val symbol: String
        get() = when (this) {
            HEART -> "♥"
            DIAMOND -> "♦"
            CLUB -> "♣"
            SPADE -> "♠"
        }
    
    val isRed: Boolean
        get() = this == HEART || this == DIAMOND
}

enum class CardRank(val representation: String, val value: Int) {
    ACE("A", 1),
    TWO("2", 2),
    THREE("3", 3),
    FOUR("4", 4),
    FIVE("5", 5),
    SIX("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE("9", 9),
    TEN("10", 0),
    JACK("J", 0),
    QUEEN("Q", 0),
    KING("K", 0)
}

data class Card(
    val suit: CardSuit,
    val rank: CardRank
) {
    val score: Int get() = rank.value
}

class BaccaratDeck {
    private var cards = mutableListOf<Card>()

    init {
        reset()
    }

    fun reset() {
        cards.clear()
        // Usually, Baccarat uses an 8-deck shoe in real casinos
        repeat(8) {
            for (suit in CardSuit.values()) {
                for (rank in CardRank.values()) {
                    cards.add(Card(suit, rank))
                }
            }
        }
        cards.shuffle()
    }

    fun draw(): Card {
        if (cards.size < 10) {
            reset() // Shuffle again if less than 10 cards left
        }
        return cards.removeAt(0)
    }

    fun cardsRemaining(): Int = cards.size
}

// Simulated active hand output
data class ActiveHand(
    val playerHand: List<Card> = emptyList(),
    val bankerHand: List<Card> = emptyList(),
    val handState: HandState = HandState.IDLE,
    val lastResult: BaccaratResult? = null
) {
    val playerScore: Int
        get() = if (playerHand.isEmpty()) 0 else playerHand.sumOf { it.score } % 10

    val bankerScore: Int
        get() = if (bankerHand.isEmpty()) 0 else bankerHand.sumOf { it.score } % 10
}

enum class HandState {
    IDLE,
    DEALT_INITIAL, // Dealt 2 cards to each
    DREW_THIRD_PLAYER,
    DREW_THIRD_BANKER,
    COMPLETED
}

// AI Strategy Predictions and Analysis Output
data class AIAnalysis(
    val recommendation: HandWinner,
    val confidence: Int,       // 0% to 100%
    val matchRate: Int,        // Success rate in this shoe
    val predictionFormula: String,
    val strengthStars: Int,    // 1 to 5
    val formulaStats: Map<String, FormulaPrediction> // Break down per formula
)

data class FormulaPrediction(
    val name: String,
    val recommendation: HandWinner?,
    val confidence: Int
)
