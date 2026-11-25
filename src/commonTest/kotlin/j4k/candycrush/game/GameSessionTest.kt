package j4k.candycrush.game

import j4k.candycrush.GameMechanics
import j4k.candycrush.math.PositionGrid.Position
import j4k.candycrush.model.Level
import j4k.candycrush.model.Level.TileObjective
import j4k.candycrush.model.Tile
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GameSessionTest {

    /** A candy source that must never be used: the tests below only rely on the reserve. */
    private val noRandom: () -> Tile = { error("Random candy requested") }

    private fun session(
        grid: String,
        reserve: String?,
        moves: Int = 10,
        objectives: List<TileObjective> = listOf(TileObjective(Tile.A, 100)),
    ) = GameSession(levelFactory = { Level(grid.trimIndent(), reserve?.trimIndent(), moves, objectives, noRandom) })

    // Swap (2,2)<->(2,1) forms A,A,A on the bottom row; the reserve then drops C,C,C which chain-react.
    private val chainGrid = """
        [D, E, D]
        [E, D, A]
        [A, A, B]
    """
    private val chainReserve = """
        [A, B, E]
        [C, C, C]
    """

    @Test
    fun startingState() {
        val game = GameSession()
        assertEquals(DefaultLevel.GRID.replace(",", ", "), game.field.toString())
        assertEquals(42, game.movesLeft)
        assertEquals(0, game.score)
        assertEquals(
            listOf(ObjectiveProgress(Tile.A, 18, 18), ObjectiveProgress(Tile.C, 22, 22)),
            game.objectives(),
        )
        assertEquals(GameStatus.PLAYING, game.status)
    }

    @Test
    fun rejectedSwapChangesNothing() {
        val game = session("[A, B, C]\n[B, C, D]", null)
        assertEquals(MoveResult.Rejected, game.play(Position(0, 0), Position(1, 0)))
        assertEquals("[A, B, C]\n[B, C, D]", game.field.toString())
        assertEquals(10, game.movesLeft)
    }

    @Test
    fun nonNeighbourOrOutsideGesturesAreIgnored() {
        val game = session(chainGrid, chainReserve)
        assertEquals(MoveResult.Ignored, game.play(Position(0, 0), Position(1, 1)))
        assertEquals(MoveResult.Ignored, game.play(Position(0, 0), Position(2, 0)))
        assertEquals(MoveResult.Ignored, game.play(Position(2, 2), Position(3, 2)))
        assertEquals(MoveResult.Ignored, game.play(Position(0, 0), Position(0, -1)))
        assertEquals(10, game.movesLeft)
    }

    @Test
    fun singleAlignmentScoresTwentyPerTile() {
        val game = session(
            """
            [B, C, D, E]
            [C, D, E, B]
            [A, A, B, A]
            """,
            "[A, B, C, H]",
        )
        val result = assertIs<MoveResult.Accepted>(game.play(Position(2, 2), Position(3, 2)))
        assertEquals(1, result.waves.size)
        assertEquals(1, result.waves[0].multiplier)
        assertEquals(60, result.waves[0].points)
        assertEquals(60, game.score)
        assertEquals(9, game.movesLeft)
        assertEquals("[A, B, C, E]\n[B, C, D, B]\n[C, D, E, B]", game.field.toString())
        assertEquals(97, game.objectives().single().remaining)
    }

    @Test
    fun chainReactionIncreasesMultiplier() {
        val game = session(chainGrid, chainReserve)
        val result = assertIs<MoveResult.Accepted>(game.play(Position(2, 2), Position(2, 1)))
        assertEquals(listOf(1, 2), result.waves.map { it.multiplier })
        assertEquals(listOf(60, 120), result.waves.map { it.points })
        assertEquals(listOf(60, 180), result.waves.map { it.scoreAfter })
        assertEquals(180, game.score)
        assertEquals("[A, B, E]\n[D, E, D]\n[E, D, B]", game.field.toString())
    }

    @Test
    fun waveDescribesRemovalDropAndRefill() {
        val game = session(chainGrid, chainReserve)
        val wave = assertIs<MoveResult.Accepted>(game.play(Position(2, 2), Position(2, 1))).waves.first()
        assertEquals(setOf(Position(0, 2), Position(1, 2), Position(2, 2)), wave.removed.map { it.position }.toSet())
        assertTrue(wave.removed.all { it.tile == Tile.A })
        assertEquals(6, wave.drops.size)
        assertEquals(
            listOf(Tile.C, Tile.C, Tile.C),
            wave.inserts.map(GameMechanics.InsertMove::tile),
        )
    }

    @Test
    fun crossingAlignmentsStartAtMultiplierTwoAndCountTilesOnce() {
        val game = session(
            """
            [B, A, C]
            [A, D, A]
            [E, A, B]
            [C, A, E]
            """,
            """
            [D, A, E]
            [H, B, H]
            [H, C, H]
            """,
        )
        val result = assertIs<MoveResult.Accepted>(game.play(Position(1, 0), Position(1, 1)))
        assertEquals(1, result.waves.size)
        assertEquals(5, result.waves[0].removed.size)
        assertEquals(2, result.waves[0].multiplier)
        assertEquals(200, game.score)
        assertEquals(95, game.objectives().single().remaining)
        assertEquals("[D, A, E]\n[B, B, C]\n[E, C, B]\n[C, D, E]", game.field.toString())
    }

    @Test
    fun objectivesCountChainReactionsAndNeverGoBelowZero() {
        val objectives = listOf(TileObjective(Tile.A, 2), TileObjective(Tile.C, 5))
        val game = session(chainGrid, chainReserve, objectives = objectives)
        game.play(Position(2, 2), Position(2, 1))
        assertEquals(listOf(0, 2), game.objectives().map { it.remaining })
        assertEquals(GameStatus.PLAYING, game.status)
    }

    @Test
    fun victoryWhenEveryObjectiveIsReached() {
        val objectives = listOf(TileObjective(Tile.A, 3), TileObjective(Tile.C, 3))
        val game = session(chainGrid, chainReserve, objectives = objectives)
        val result = assertIs<MoveResult.Accepted>(game.play(Position(2, 2), Position(2, 1)))
        assertEquals(GameStatus.WON, result.status)
        assertEquals(GameStatus.WON, game.status)
    }

    @Test
    fun victoryWinsOnTheLastMove() {
        val game = session(chainGrid, chainReserve, moves = 1, objectives = listOf(TileObjective(Tile.A, 3)))
        game.play(Position(2, 2), Position(2, 1))
        assertEquals(0, game.movesLeft)
        assertEquals(GameStatus.WON, game.status)
    }

    @Test
    fun defeatWhenNoMoveLeftAndObjectiveMissed() {
        val game = session(chainGrid, chainReserve, moves = 1, objectives = listOf(TileObjective(Tile.A, 4)))
        game.play(Position(2, 2), Position(2, 1))
        assertEquals(GameStatus.LOST, game.status)
        assertEquals(MoveResult.Ignored, game.play(Position(0, 0), Position(1, 0)))
    }

    @Test
    fun restartReloadsTheLevelAndResetsCounters() {
        val game = session(chainGrid, chainReserve, moves = 1, objectives = listOf(TileObjective(Tile.A, 4)))
        game.play(Position(2, 2), Position(2, 1))
        game.restart()
        assertEquals(chainGrid.trimIndent(), game.field.toString())
        assertEquals(chainReserve.trimIndent(), game.level.reserve.toString())
        assertEquals(0, game.score)
        assertEquals(1, game.movesLeft)
        assertEquals(4, game.objectives().single().remaining)
        assertEquals(GameStatus.PLAYING, game.status)
    }

    @Test
    fun shuffleGivesAGridWithoutAlignmentAndResetsCounters() {
        val random = Random(SEED)
        val randomTiles = { Tile.randomTile(random) }
        val game = GameSession(levelFactory = { DefaultLevel.create(randomTiles) }, randomTiles = randomTiles)
        game.play(Position(2, 3), Position(3, 3))
        game.shuffle()
        assertTrue(GameMechanics(game.field).findAllAlignedCells().isEmpty())
        assertTrue(game.field.positions().all { game.field[it].isTile() })
        assertEquals(0, game.score)
        assertEquals(DefaultLevel.MOVES, game.movesLeft)
        assertEquals(listOf(18, 22), game.objectives().map { it.remaining })
    }

    private companion object {
        const val SEED = 7
    }
}

class ScoreRulesTest {
    @Test
    fun wavePoints() {
        assertEquals(60, ScoreRules.wavePoints(removedTiles = 3, multiplier = 1))
        assertEquals(240, ScoreRules.wavePoints(removedTiles = 4, multiplier = 3))
    }

    @Test
    fun multipliers() {
        assertEquals(1, ScoreRules.firstWaveMultiplier(1))
        assertEquals(2, ScoreRules.firstWaveMultiplier(2))
        assertEquals(2, ScoreRules.firstWaveMultiplier(3))
        assertEquals(3, ScoreRules.nextWaveMultiplier(2))
    }
}

class FieldGeneratorTest {
    @Test
    fun randomFieldsNeverContainAlignments() {
        repeat(SEEDS) { seed ->
            val random = Random(seed)
            val field = FieldGenerator.randomFieldWithoutAlignment(5, 9) { Tile.randomTile(random) }
            assertTrue(GameMechanics(field).findAllAlignedCells().isEmpty(), "seed $seed:\n$field")
        }
    }

    @Test
    fun drawIsRepeatedWhileItFormsAnAlignment() {
        val supply = ArrayDeque(listOf(Tile.A, Tile.A, Tile.A, Tile.B))
        val field = FieldGenerator.randomFieldWithoutAlignment(3, 1) { supply.removeFirst() }
        assertEquals("[A, A, B]", field.toString())
    }

    private companion object {
        const val SEEDS = 50
    }
}
