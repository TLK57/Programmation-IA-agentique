import j4k.candycrush.GameMechanics
import j4k.candycrush.game.*
import j4k.candycrush.math.PositionGrid.Position
import j4k.candycrush.model.*
import kotlin.random.Random

fun check(nom: String, ok: Boolean, detail: Any? = "") = println((if (ok) "OK   " else "ECART ") + nom + (if (detail != "" ) "  -> $detail" else ""))

fun main() {
    // 1. Coups valides au départ sur le niveau du cahier des charges, et le premier coup annoncé à 240
    val g = GameField.fromString(DefaultLevel.GRID)
    val m = GameMechanics(g)
    val coups = mutableListOf<Pair<Position, Position>>()
    for (c in 0 until 5) for (r in 0 until 9) {
        val a = Position(c, r)
        for (b in listOf(Position(c + 1, r), Position(c, r + 1))) if (m.isSwapAllowed(a, b)) coups += a to b
    }
    check("grille de départ sans alignement", m.findAllAlignedCells().isEmpty())
    println("     coups valides au départ : ${coups.size}")
    for ((a, b) in coups) {
        val s = GameSession()
        val res = s.play(a, b) as MoveResult.Accepted
        println("     coup $a<->$b : vagues=${res.waves.map { "${it.removed.size}x${it.multiplier}=${it.points}" }} score=${s.score} coups=${s.movesLeft} obj=${s.objectives().map { it.remaining }}")
    }
    // 2. EF-02 / EQ-10 : gestes hors grille, non voisins, diagonale
    val s = GameSession()
    val avant = s.field.toString()
    val bad = listOf(Position(-1, 0) to Position(0, 0), Position(4, 8) to Position(5, 8), Position(0, 0) to Position(1, 1), Position(0, 0) to Position(2, 0), Position(0, 0) to Position(0, 0))
    check("gestes invalides ignorés", bad.all { s.play(it.first, it.second) == MoveResult.Ignored } && s.field.toString() == avant && s.movesLeft == 42)
    // 3. EF-04 : échange refusé ne compte pas
    val s3 = GameSession(); val inv = (0 until 4).map { Position(it, 8) to Position(it + 1, 8) }.first { !GameMechanics(GameField.fromString(DefaultLevel.GRID)).isSwapAllowed(it.first, it.second) }; val refus = s3.play(inv.first, inv.second)
    check("échange refusé : grille et coups inchangés", refus == MoveResult.Rejected && s3.movesLeft == 42 && s3.field.toString() == avant, refus)
    // 4. EF-32 : mélange sans alignement (2000 tirages)
    var mauvais = 0
    repeat(2000) { val f = FieldGenerator.randomFieldWithoutAlignment(5, 9) { Tile.randomTile() }; if (GameMechanics(f).findAllAlignedCells().isNotEmpty() || f.positions().any { p -> f[p].isNotTile() }) mauvais++ }
    check("mélange : aucune grille avec alignement sur 2000", mauvais == 0, mauvais)
    // 5. EF-31 : rejouer remet la réserve
    val s5 = GameSession(); s5.play(coups[0].first, coups[0].second); s5.restart()
    check("rejouer remet grille, réserve, score, coups", s5.field.toString() == avant && s5.level.reserve.toString() == GameField.fromString(DefaultLevel.RESERVE).toString() && s5.score == 0 && s5.movesLeft == 42)
    // 6. Robustesse : 3000 parties au hasard jusqu'à la fin
    val rnd = Random(57); var erreurs = 0; var won = 0; var lost = 0; var maxVagues = 0; var apresFin = 0; var bloquees = 0
    repeat(3000) { n ->
        try {
            val r = Random(n)
            val ses = GameSession(randomTiles = { Tile.randomTile(r) }, levelFactory = { DefaultLevel.create { Tile.randomTile(r) } })
            var garde = 0
            while (ses.status == GameStatus.PLAYING && garde++ < 200000) {
                val a = Position(rnd.nextInt(-1, 6), rnd.nextInt(-1, 10))
                val b = listOf(Position(a.column + 1, a.row), Position(a.column, a.row + 1), Position(a.column - 1, a.row), Position(a.column, a.row - 1)).random(rnd)
                val res = ses.play(a, b)
                if (res is MoveResult.Accepted) {
                    maxVagues = maxOf(maxVagues, res.waves.size)
                    val f = ses.field
                    if (f.positions().any { f[it].isNotTile() } || GameMechanics(f).findAllAlignedCells().isNotEmpty()) erreurs++
                    if (res.waves.first().multiplier !in 1..2 || res.waves.zipWithNext().any { (x, y) -> y.multiplier != x.multiplier + 1 }) erreurs++
                    if (res.waves.sumOf { it.points } != res.waves.sumOf { it.removed.size * 20 * it.multiplier }) erreurs++
                    if (res.waves.any { w -> w.removed.map { it.position }.toSet().size != w.removed.size }) erreurs++
                }
                
            }
            if (ses.status == GameStatus.WON) won++ else if (ses.status == GameStatus.LOST) lost++
            if (ses.status != GameStatus.PLAYING && coups.any { ses.play(it.first, it.second) != MoveResult.Ignored }) apresFin++
            if (ses.status == GameStatus.PLAYING) { val mm = GameMechanics(ses.field); val possibles = (0 until 5).flatMap { c -> (0 until 9).flatMap { r -> listOf(Position(c+1,r), Position(c,r+1)).filter { mm.isSwapAllowed(Position(c,r), it) } } }.size; if (possibles == 0) bloquees++ else erreurs++ }
            if (ses.status == GameStatus.LOST && ses.movesLeft != 0) erreurs++
            if (ses.objectives().any { it.remaining < 0 }) erreurs++
        } catch (e: Throwable) { erreurs++; if (erreurs < 3) println("     exception partie $n : $e") }
    }
    check("3000 parties au hasard sans plantage ni incohérence", erreurs == 0, "erreurs=$erreurs victoires=$won défaites=$lost vagues max=$maxVagues grilles bloquées=$bloquees")
    check("aucun coup accepté après la fin", apresFin == 0, apresFin)
    // 7. Cas limite : niveau sans objectif (paramètres par défaut de Level)
    val s7 = GameSession(levelFactory = { Level(DefaultLevel.GRID, DefaultLevel.RESERVE) })
    s7.play(coups[0].first, coups[0].second)
    println("     niveau sans objectif, après 1 coup : statut=${s7.status}, coups par défaut=${Level(DefaultLevel.GRID).maxMoves}")
}
