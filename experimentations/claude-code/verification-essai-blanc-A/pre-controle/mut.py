import subprocess, shutil, os, sys
B=os.path.expanduser("~/verif"); S=B+"/essai/src/commonMain/kotlin/j4k/candycrush/"
L=B+"/kotlinc/lib"
env=dict(os.environ, PATH="/usr/lib/jvm/java-17-openjdk-amd64/bin:"+os.environ["PATH"], JAVA_TOOL_OPTIONS="", _JAVA_OPTIONS="", JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64")
M=[
("M01 isSwapAllowed ne remet pas la grille","GameMechanics.kt","        swapTiles(a, b)\n        return allowed","        return allowed"),
("M02 isSwapAllowed: ET au lieu de OU","GameMechanics.kt","isInRowWithThree(a) || isInRowWithThree(b)","isInRowWithThree(a) && isInRowWithThree(b)"),
("M03 alignement minimum 4","GameMechanics.kt","const val MIN_ALIGNMENT = 3","const val MIN_ALIGNMENT = 4"),
("M04 getConnectedTiles horizontal avant vertical","GameMechanics.kt","listOf(getVerticalSurroundings(pos), getHorizontalSurroundings(pos))","listOf(getHorizontalSurroundings(pos), getVerticalSurroundings(pos))"),
("M05 chute: pas de trou laisse","GameMechanics.kt","        field[move.tile] = Tile.Hole\n","\n"),
("M06 insertions de haut en bas","GameMechanics.kt","compareByDescending<Position> { it.row }","compareBy<Position> { it.row }"),
("M07 points par bonbon 10","game/Rules.kt","POINTS_PER_TILE = 20","POINTS_PER_TILE = 10"),
("M08 x2 jamais applique","game/Rules.kt","if (alignmentCount > 1) MULTI_ALIGNMENT_MULTIPLIER else BASE_MULTIPLIER","BASE_MULTIPLIER"),
("M09 chaine: multiplicateur constant","game/Rules.kt","fun nextWaveMultiplier(previous: Int): Int = previous + 1","fun nextWaveMultiplier(previous: Int): Int = previous"),
("M10 victoire seulement si collecte > objectif","game/GameSession.kt","ObjectiveProgress(it.tile, it.goal, max(0, it.goal - (collected[it.tile] ?: 0)))","ObjectiveProgress(it.tile, it.goal, max(0, it.goal - (collected[it.tile] ?: 0) + 1))"),
("M11 defaite prioritaire sur victoire","game/GameSession.kt","        objectives().all { it.remaining == 0 } -> GameStatus.WON\n        movesLeft == 0 -> GameStatus.LOST","        movesLeft == 0 -> GameStatus.LOST\n        objectives().all { it.remaining == 0 } -> GameStatus.WON"),
("M12 coup non decompte","game/GameSession.kt","movesLeft = max(0, movesLeft - 1)","movesLeft = max(0, movesLeft - 0)"),
("M13 reserve: pas de descente","model/Level.kt","            reserveField[column, row] = reserveField[column, row - 1]\n","\n"),
("M14 reserve prise par le haut","model/Level.kt","(reserveField.rowSize - 1 downTo 0).firstOrNull","(0 until reserveField.rowSize).firstOrNull"),
("M15 clone partage les lignes","model/GameField.kt","copy.rows[index] = row.copy()","copy.rows[index] = row"),
("M16 melange ne verifie pas la colonne","game/FieldGenerator.kt"," || before.all { field[column, row - it] == tile }",""),
("M17 chaine non resolue","game/GameSession.kt","toRemove = mechanics.findAllAlignedCells()","toRemove = emptyList()"),
("M18 croisement compte deux fois","game/GameSession.kt","swapAlignments.flatten().distinct()","swapAlignments.flatten()"),
("M19 getField sans decalage","math/PositionGrid.kt","Position((posX - x) / tileSize, (posY - y) / tileSize)","Position(posX / tileSize, posY / tileSize)"),
("M20 Row toString sans espace","model/Row.kt",'joinToString(", ", "[", "]")','joinToString(",", "[", "]")'),
]
def run(jar):
    try:
      r=subprocess.run(["java","-cp",f"{B}/out/{jar}.jar:{B}/out/m.jar:{B}/out/runner.jar:{L}/kotlin-stdlib.jar:{L}/kotlin-test.jar","RunnerKt",f"{B}/out/{jar}.txt"],capture_output=True,text=True,env=env,timeout=60)
    except subprocess.TimeoutExpired: return "BOUCLE"
    last=[l for l in r.stdout.splitlines() if l.startswith("TOTAL")]
    return last[0].split("echec=")[1] if last else "ERR"
logic=[l for l in subprocess.run(f"cd {B} && find essai/src/commonMain/kotlin -name '*.kt' -not -path '*/view/*' -not -name main.kt",shell=True,capture_output=True,text=True).stdout.split()]
print(f"{'mutation':50} tests IA (echecs) | tests auteur (echecs)")
import sys
for name,f,old,new in M[int(sys.argv[1]) if len(sys.argv)>1 else 0:]:
    p=S+f; src=open(p).read(); assert src.count(old)==1,(name,src.count(old))
    open(p,"w").write(src.replace(old,new))
    try:
        c=subprocess.run([B+"/kotlinc/bin/kotlinc","-Xallow-kotlin-package","-cp",L+"/kotlin-test.jar"]+[B+"/stubs/Stubs.kt",B+"/stubs/Point.kt"]+[B+"/"+x for x in logic]+["-d",B+"/out/m.jar"],capture_output=True,text=True,env=env)
        if c.returncode: print(name,"NE COMPILE PAS"); continue
        print(f"{name:50} {run('ia'):>5} | {run('ref'):>5}",flush=True)
    finally: open(p,"w").write(src)
