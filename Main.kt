package chess

class ChessBoard {
    companion object {
        val alignmentToIndex: HashMap<String, Int> = hashMapOf(
            "a" to 1,
            "b" to 2,
            "c" to 3,
            "d" to 4,
            "e" to 5,
            "f" to 6,
            "g" to 7,
            "h" to 8
        )
        val indexToAlignment: HashMap<Int, String> = hashMapOf(
            1 to "a",
            2 to "b",
            3 to "c",
            4 to "d",
            5 to "e",
            6 to "f",
            7 to "g",
            8 to "h"
        )
        val chessBoard: MutableList<MutableList<String>> = MutableList(8) { MutableList(8) { " " } }

        var chessPawnW = mutableListOf<Pair<String, ChessGame.Pawn>>()
        var chessPawnB = mutableListOf<Pair<String, ChessGame.Pawn>>()
        var stackExtentionPosition = mutableListOf<Pair<String, ChessGame.Pawn>>()

        init {
            for (i in 'a'..'h') {
                chessPawnW.add(i.toString() + 2 to ChessGame.Pawn(i.toString() + 2, "W", i.toString() + 2))
                chessPawnB.add(i.toString() + 7 to ChessGame.Pawn(i.toString() + 7, "B", i.toString() + 7))
            }
            chessBoard.freshBoard()

        }


        fun MutableList<MutableList<String>>.freshBoard(
            _chessBoard: MutableList<MutableList<String>> = toMutableList()
        ): MutableList<MutableList<String>> {
            for (b in chessPawnB) {
                _chessBoard[b.first.substring(1, 2).toInt() - 1][alignmentToIndex[b.first.substring(
                    0,
                    1
                )]!!.toInt() - 1] = "B"
            }
            for (w in chessPawnW) {
                _chessBoard[w.first.substring(1, 2).toInt() - 1][alignmentToIndex[w.first.substring(
                    0,
                    1
                )]!!.toInt() - 1] = "W"
            }
            return _chessBoard
        }

        fun printBoard(
            _chessBoard: MutableList<MutableList<String>> = chessBoard
        ) {

            println("  +---+---+---+---+---+---+---+---+")
            for (i in _chessBoard.size downTo 1) {
                println("$i | ${_chessBoard[i - 1].joinToString(" | ")} |")
                println("  +---+---+---+---+---+---+---+---+")
            }
            println("    a   b   c   d   e   f   g   h  ")
            println()
        }

    }

    //--------------------- ChessPlayer -------------------------------------------------------------------------------
    class ChessPlayer(_number: Int = 1) {


        var number: Int = _number
        var name: String = "Unknown"

        init {
            if (number == 1 || number == 2) {
                println("${if (number == 1) "First" else "Second"} Player's name:")
                name = readln()
            }
        }

        var chessChar: String = if (number == 1) "W" else if (number == 2) "B" else "empty"
        var chessColor: String =
            if (chessChar == "W") "White" else if (chessChar == "B") "Black" else "empty"


    }

    //--------------------- ChessGame -------------------------------------------------------------------------------
    class ChessGame {
        init {
            println("Pawns-Only Chess")
        }

        var step: String = "play"
        var activePlayer: ChessPlayer = ChessPlayer(1)
            set(value) {
                if (value != activePlayer) {
                    deActivePlayer = activePlayer
                    field = value
                }
            }
        var deActivePlayer: ChessPlayer = ChessPlayer(2)

        fun chessGameStepByStep() {
            //println("Pawns-Only Chess")

            printBoard()
            loop@ while (step != "exit") {
                println("${activePlayer.name}\'s turn:")
                step = readln()
                if (step == "exit") {
                    break
                }
                if (!step.isInputCheck()) {
                    continue
                }
                if (!step.startStepCheck()) {
                    continue
                }
                if (!step.positionToPawn().isPossibleMove(step)) {
                    continue
                }
                /*if (!step.positionToPawn().isMove()) {
                    println("${deActivePlayer.name}\' WIN!")
                    break
                }*/
                step.move()
                printBoard()
                var chessPawn = if (activePlayer.chessChar == "W") chessPawnW else chessPawnB

                if (chessPawn.any { it.first.matches("[a-h][1,8]".toRegex()) }) {
                    println("${activePlayer.chessColor} Wins!")
                    break
                }

                activePlayer = deActivePlayer

                chessPawn = if (activePlayer.chessChar == "W") chessPawnW else chessPawnB
                if (chessPawn.isEmpty()) {
                    println("${deActivePlayer.chessColor} Wins!")
                    break
                }
                if (chessPawn.filter { it.second.isMove() }.isEmpty()) {
                    println("Stalemate!")
                    break
                }

            }
            println("Bye!")
        }

        fun isMove(
            _chessPawn: MutableList<Pair<String, Pawn>>
        ): Boolean {



            return _chessPawn.any { it.second.isMove() }
        }



        fun String.move(
            _step: String = toString(),
            _activPlayer: ChessPlayer = activePlayer
        ) {
            val startPosition = _step.substring(0, 2)
            val newPosition = _step.substring(2, 4)

            val chessPawnDeactive = if (_activPlayer.chessChar == "W") chessPawnB else chessPawnW
            val chessPawnActive = if (_activPlayer.chessChar == "W") chessPawnW else chessPawnB



            if (chessPawnActive.any { it.first == startPosition }) {
                val index = chessPawnActive.indexOf(chessPawnActive.first { it.first == startPosition })
                if (stackExtentionPosition.any()) {
                    val extenValue = stackExtentionPosition.first()
                    if (chessPawnActive.any { it == extenValue }) {
                        chessPawnActive.remove(extenValue)

                        if (_activPlayer.chessChar == "W") {
                            val pos = extenValue.first + extenValue.first.substring(0,1) + (extenValue.first.substring(1,2).toInt() + 1)
                            freshPosiblePosition(pos) { a, b -> a + b }
                        } else {
                            val pos = extenValue.first + extenValue.first.substring(0,1) + (extenValue.first.substring(1,2).toInt() - 1)
                            freshPosiblePosition(pos) { a, b -> a - b }
                        }
                        stackExtentionPosition.remove(extenValue)
                    }
                }



                if (chessPawnActive[index].second.pawnFirstMove) {
                    chessPawnActive[index].second.pawnFirstMove = false


                    val newPosMin = if (_activPlayer.chessChar == "W") {
                        startPosition.substring(1, 2).toInt() + 1
                    } else newPosition.substring(1, 2).toInt()
                    val newPosMax = if (_activPlayer.chessChar == "W") {
                        newPosition.substring(1, 2).toInt()
                    } else startPosition.substring(1, 2).toInt() - 1


                    for (newPos in newPosMin..newPosMax) {
                        val newposition = newPosition.substring(0, 1) + newPos
                        if (newposition != newPosition) {
                            stackExtentionPosition.add(newposition to chessPawnActive[index].second)
                            chessPawnActive.add(newposition to chessPawnActive[index].second)
                        } else chessPawnActive[index] = newPosition to chessPawnActive[index].second
                        //chessPawnActive[index] = newposition to chessPawnActive[index].second
                    }
                } else chessPawnActive[index] = newPosition to chessPawnActive[index].second
                chessPawnActive[index].second.position = newPosition

                val startV = startPosition.substring(1, 2).toInt() - 1
                val startH = alignmentToIndex[startPosition.substring(0, 1)]!!.toInt() - 1
                chessBoard[startV][startH] = " "
            }
            if (chessPawnDeactive.any { it.first == newPosition }) {
                val pawnNewPosition = chessPawnDeactive.first { it.first == newPosition }.second
                val pairCoordinatePawn = chessPawnDeactive.filter { it.second == pawnNewPosition }

                for (i in pairCoordinatePawn) {
                    val position = chessPawnDeactive.first { it.second == i.second }.first
                    val startV = position.substring(1, 2).toInt() - 1
                    val startH = alignmentToIndex[position.substring(0, 1)]!!.toInt() - 1
                    chessBoard[startV][startH] = " "
                    chessPawnDeactive.remove(position to i.second)
                }
            }
            val newV = newPosition.substring(1, 2).toInt() - 1
            val newH = alignmentToIndex[newPosition.substring(0, 1)]!!.toInt() - 1
            chessBoard[newV][newH] = _activPlayer.chessChar

            if (_activPlayer.chessChar == "W") {
                freshPosiblePosition(_step) { a, b -> a + b }
            } else freshPosiblePosition(_step) { a, b -> a - b }
        }

        fun freshPosiblePosition(
            _step: String,
            operation: (a: Int, b: Int) -> Int
        ) {
            val positionChek = /*if (_step.substring(2, 4).isEmpty()) {
                mutableListOf(_step.substring(0, 2))
            } else*/ mutableListOf(_step.substring(0, 2), _step.substring(2, 4))

            for (_position in positionChek) {
                val v = _position.substring(1, 2).toInt()
                val h = _position.substring(0, 1)
                val indexh = alignmentToIndex[h]!!.toInt()

                val hmax = (if (indexh + 1 in 1..8) indexToAlignment[indexh + 1] else h)!!.first()
                val hmin = (if (indexh - 1 in 1..8) indexToAlignment[indexh - 1] else h)!!.first()
                val vmax = if (operation(v, 2) in 1..8) operation(v, 2)
                else if (operation(v, 1) in 1..8) operation(v, 1) else v

                val chessPawn = chessPawnW + chessPawnB

                when (_position) {
                    positionChek.first() -> {

                        val vmin =
                            if (operation(v, -2) in 1..8) {
                                operation(v, -2)
                            }
                            else {
                                if (operation(v, -1) in 1..8) {
                                    operation(v, -1)
                                } else v
                            }
                            val vrange = if (vmax > vmin) vmin..vmax else vmax..vmin

                        for (i in vrange) {
                            when (i) {
                                operation(v, -2) -> {
                                    val pos = h + i
                                    if (pos !in positionChek && chessPawn.any { it.first == pos }) {
                                        val index = chessPawn.indexOf(chessPawn.first { it.first == pos })
                                        chessPawn[index].second.possiblePosition =
                                            chessPawn[index].second.possiblePosition()
                                    }
                                }
                                v -> {
                                    continue
                                }
                                else -> {
                                    val pawnPosCheck =
                                        chessPawn[chessPawn.indexOf(chessPawn.first { it.first == positionChek.last() })].second
                                    for (j in hmin..hmax) {
                                        val pos = j.toString() + i
                                        if (chessPawn.any { it.first == pos }) {
                                            val index = chessPawn.indexOf(chessPawn.first { it.first == pos })
                                            val pawnPos = chessPawn[index].second
                                            if (pos !in positionChek && pawnPos != pawnPosCheck) {
                                                chessPawn[index].second.possiblePosition =
                                                    chessPawn[index].second.possiblePosition()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {

                        val vmin = if (operation(v, 1) in 1..8) operation(v, 1) else v
                        val vrange = if (vmax > vmin) vmin..vmax else vmax..vmin
                        for (i in vrange) {
                            when (i) {
                                operation(v, 2) -> {
                                    val pos = h + i
                                    if (pos !in positionChek && chessPawn.any { it.first == pos }) {
                                        val index = chessPawn.indexOf(chessPawn.first { it.first == pos })
                                        chessPawn[index].second.possiblePosition =
                                            chessPawn[index].second.possiblePosition()
                                    }
                                }
                                v -> {
                                    continue
                                }
                                else -> {
                                    for (j in hmin..hmax) {
                                        val pos = j.toString() + i
                                        if (chessPawn.any { it.first == pos }) {
                                            val index = chessPawn.indexOf(chessPawn.first { it.first == pos })
                                            chessPawn[index].second.possiblePosition =
                                                chessPawn[index].second.possiblePosition()
                                        }
                                    }
                                }
                            }
                        }
                    }


                }

            }

        }


        fun String.startStepCheck(
            _step: String = toString(),
            _activPlayer: ChessPlayer = activePlayer
        ): Boolean {
            var status = true
            val startPositionH = alignmentToIndex[_step[0].toString()]!!.toInt() - 1
            val startPositionV = _step[1].digitToInt() - 1
            val chessPawn = if (_activPlayer.chessChar == "W") chessPawnB else chessPawnW
            if (chessPawn.any { it.first == _step.substring(0, 2) } ||
                !(chessPawnB.any { it.first == _step.substring(0, 2) }) &&
                !(chessPawnW.any { it.first == _step.substring(0, 2) })
            ) {
                println("No ${_activPlayer.chessColor} pawn at ${indexToAlignment[startPositionH + 1]}${startPositionV + 1}")
                status = false
            }
            return status
        }

        fun String.positionToPawn(
            _step: String = toString(),
            _activPlayer: ChessPlayer = activePlayer
        ): Pawn {
            val chessPawn = if (_activPlayer.chessChar == "W") chessPawnW else chessPawnB
            return if (chessPawn.any { it.first == _step.substring(0, 2) }) {
                chessPawn.first { it.first == _step.substring(0, 2) }.second
            } else Pawn("Unknown", "Unknown", "Unknown")
        }

        fun String.isInputCheck(
            _step: String = toString()
        ): Boolean {
            //val status = true
            val isRightStep = "[a-h][1-8][a-h][1-8]".toRegex()
            if (_step == "exit") {
                return true
            }
            if (!_step.matches(isRightStep)) {
                println("Invalid Input")
                return false
            }
            return true
        }


// ---------------------------- Pawn ---------------------------------------------------------------------

        class Pawn(_name: String, _chessChar: String, _position: String) {
            val name = _name
            val chessChar: String = _chessChar
            var pawnFirstMove = true
            var position = _position
                set(value) {
                    possiblePosition = possiblePosition(value)
                    field = value
                }
            var possiblePosition: MutableList<String> = possiblePosition(position)


            fun isPossibleMove(newPosition: String): Boolean {
                return if (newPosition !in possiblePosition) {
                    println("Invalid Input")
                    false
                } else true
            }

            fun isMove(): Boolean {
                return possiblePosition.any()
            }

            fun possiblePosition(
                _position: String = position,
            ): MutableList<String> {
                val chessDeactive = if (chessChar == "W") chessPawnB else chessPawnW
                val chessActive = if (chessChar == "W") chessPawnW else chessPawnB
                if (chessChar == "W")
                    return posiblePositionList(_position, chessActive, chessDeactive) { a, b -> a + b } else
                    return posiblePositionList(_position, chessActive, chessDeactive) { a, b -> a - b }

            }


            private fun posiblePositionList(
                _position: String,
                _chessActive: MutableList<Pair<String, Pawn>>,
                _chessDeactive: MutableList<Pair<String, Pawn>>,
                _operation: (Int, Int) -> Int
            ): MutableList<String> {
                val possiblePositionList = mutableListOf<String>()
                val positionH = _position.first()
                val positionV = _position[1].digitToInt()
                val vmax = if (pawnFirstMove) 2 else 1
                val hmin = if (positionH - 1 in 'a'..'h') positionH - 1 else positionH
                val hmax = if (positionH + 1 in 'a'..'h') positionH + 1 else positionH

                for (v in 1..vmax) {
                    if (v == 1) {
                        for (h in hmin..hmax) {
                            val pos = h.toString() + _operation(positionV, v)
                            if (
                                h == positionH && (
                                        !(_chessActive.any { it.first == pos }) &&
                                                !(_chessDeactive.any { it.first == pos })
                                        )
                            ) {
                                possiblePositionList.add(_position + pos)
                            } else if (
                                h != positionH && (
                                        _chessDeactive.any { it.first == pos }
                                        )
                            ) {
                                possiblePositionList.add(_position + pos)
                            }
                        }
                    } else {
                        //val h = positionH
                        val pos1 = positionH.toString() + _operation(positionV, v - 1)
                        val pos2 = positionH.toString() + _operation(positionV, v)
                        if (
                            (
                                    !(_chessActive.any { it.first == pos1 }) &&
                                            !(_chessDeactive.any { it.first == pos1 })
                                    ) && (
                                    !(_chessActive.any { it.first == pos2 }) &&
                                            !(_chessDeactive.any { it.first == pos2 })
                                    )
                        ) possiblePositionList.add(_position + pos2)
                    }
                }
                return possiblePositionList
            }
        }
    }





}


fun main() {
ChessBoard.ChessGame().chessGameStepByStep()
}











