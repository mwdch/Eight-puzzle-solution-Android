package com.mwdch.eight_puzzlesolution

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import com.mwdch.eight_puzzlesolution.databinding.ActivityMainBinding
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.abs

const val TAG = "MainOperations"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val goalState = listOf(" ", "1", "2", "3", "4", "5", "6", "7", "8")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val startState = mutableListOf(" ", "1", "4", "7", "5", "2", "3", "6", "8")
        startState.shuffle()
        setStartState(startState)

        binding.btnChangeStartState.setOnClickListener {
            startState.shuffle()
            setStartState(startState)
            binding.tvEmptyState.text = getString(R.string.empty_view)
        }

        binding.btnStartOperation.setOnClickListener {
            if (isSolvable(startState)) {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvEmptyState.visibility = View.GONE
                val startTime = System.currentTimeMillis()
                Log.i(TAG, "Start...")
                thread(start = true) {
                    var result: Result? = null
                    when (binding.rgAlgorithm.checkedRadioButtonId) {
                        R.id.rbUniformCost -> result = uniformCostSearch(startState, goalState)
                        R.id.rbIterativeDeepening -> result =
                            iterativeDeepeningSearch(startState, goalState)
                        R.id.rbAStar -> {
                            when (binding.rgHeuristic.checkedRadioButtonId) {
                                R.id.rbHeuristic1 -> result = aStarSearch(startState, goalState, 1)
                                R.id.rbHeuristic2 -> result = aStarSearch(startState, goalState, 2)
                            }
                        }

                        R.id.rbIDAStar -> {
                            when (binding.rgHeuristic.checkedRadioButtonId) {
                                R.id.rbHeuristic1 -> result = iterativeDeepeningAStarSearch(
                                    startState,
                                    goalState,
                                    1
                                )
                                R.id.rbHeuristic2 -> result = iterativeDeepeningAStarSearch(
                                    startState,
                                    goalState,
                                    2
                                )
                            }
                        }
                    }
                    Log.i(TAG, result?.path.toString())
                    Log.i(TAG, "End.")

                    runOnUiThread {
                        //get time
                        val difference: Long = System.currentTimeMillis() - startTime
                        val days = (difference / (1000 * 60 * 60 * 24)).toInt()
                        var hours =
                            ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60)).toInt()
                        var minutes =
                            (difference - 1000 * 60 * 60 * 24 * days - 1000 * 60 * 60 * hours).toInt() / (1000 * 60)
                        var seconds =
                            (difference - 1000 * 60 * 60 * 24 * days - 1000 * 60 * 60 * hours - 1000 * 60 * minutes)
                                .toInt() / (1000)
                        var milliseconds =
                            (difference - 1000 * 60 * 60 * 24 * days - 1000 * 60 * 60 * hours - 1000 * 60 * minutes - 1000 * seconds).toInt()
                        hours = if (hours < 0) -hours else hours
                        minutes = if (minutes < 0) -minutes else minutes
                        seconds = if (seconds < 0) -seconds else seconds
                        milliseconds = if (milliseconds < 0) -milliseconds else milliseconds

                        binding.tvEmptyState.text =
                            "$hours hours, $minutes minutes, $seconds seconds, $milliseconds milliseconds\n" +
                                    if (result?.path!!.isEmpty()) "Current state is goal" else result?.path
                        binding.tvActionNumber.text = "Action: " + result.action
                        binding.tvDepth.text = "Depth: " + result.depth
                        binding.tvExpandedNodes.text = "ExpandedNodes: " + result.expandedNodes
                        binding.tvCost.text = "Cost: " + result.cost
                        binding.progressBar.visibility = View.GONE
                        binding.tvEmptyState.visibility = View.VISIBLE
                    }
                }
            } else {
                binding.tvEmptyState.text = "No answer"
            }
        }
    }

    private fun View.fadeIn() {
        this.startAnimation(AlphaAnimation(0.2F, 1F).apply {
            duration = 700
            fillAfter = true
        })
    }

    private fun setStartState(startState: MutableList<String>) {
        binding.index0.text = startState[0]
        binding.index0.fadeIn()
        binding.index1.text = startState[1]
        binding.index1.fadeIn()
        binding.index2.text = startState[2]
        binding.index2.fadeIn()
        binding.index3.text = startState[3]
        binding.index3.fadeIn()
        binding.index4.text = startState[4]
        binding.index4.fadeIn()
        binding.index5.text = startState[5]
        binding.index5.fadeIn()
        binding.index6.text = startState[6]
        binding.index6.fadeIn()
        binding.index7.text = startState[7]
        binding.index7.fadeIn()
        binding.index8.text = startState[8]
        binding.index8.fadeIn()
    }

/*
indexes structure
0  1  2
3  4  5
6  7  8
 */

    private fun goDown(state: MutableList<String>): MutableList<String>? {
        val newState = state.toMutableList()
        val zeroIndex = newState.indexOf(" ")
        return if (zeroIndex in listOf(6, 7, 8)) {
            null //We can't go down
        } else {
            val temp = state[zeroIndex + 3]
            newState[zeroIndex + 3] = newState[zeroIndex]
            newState[zeroIndex] = temp
            newState
        }
    }

    private fun goUp(state: MutableList<String>): MutableList<String>? {
        val newState = state.toMutableList()
        val zeroIndex = newState.indexOf(" ")
        return if (zeroIndex in listOf(0, 1, 2)) {
            null //We can't go up
        } else {
            val temp = state[zeroIndex - 3]
            newState[zeroIndex - 3] = newState[zeroIndex]
            newState[zeroIndex] = temp
            newState
        }
    }

    private fun goRight(state: MutableList<String>): MutableList<String>? {
        val newState = state.toMutableList()
        val zeroIndex = newState.indexOf(" ")
        return if (zeroIndex in listOf(2, 5, 8)) {
            null //We can't go right
        } else {
            val temp = state[zeroIndex + 1]
            newState[zeroIndex + 1] = newState[zeroIndex]
            newState[zeroIndex] = temp
            newState
        }
    }

    private fun goLeft(state: MutableList<String>): MutableList<String>? {
        val newState = state.toMutableList()
        val zeroIndex = newState.indexOf(" ")
        return if (zeroIndex in listOf(0, 3, 6)) {
            null //We can't go left
        } else {
            val temp = state[zeroIndex - 1]
            newState[zeroIndex - 1] = newState[zeroIndex]
            newState[zeroIndex] = temp
            newState
        }
    }

    private fun expandNode(node: Node): MutableList<Node> {
        val nodes = mutableListOf<Node>()
        var newState = goDown(node.state)
        if (newState != null)
            nodes.add(Node(newState, node, "Down", node.depth + 1, 1))
        newState = goUp(node.state)
        if (newState != null)
            nodes.add(Node(newState, node, "Up", node.depth + 1, 1))
        newState = goLeft(node.state)
        if (newState != null)
            nodes.add(Node(newState, node, "Left", node.depth + 1, 1))
        newState = goRight(node.state)
        if (newState != null)
            nodes.add(Node(newState, node, "Right", node.depth + 1, 1))
        return nodes
    }

    private fun uniformCostSearch(
        startState: MutableList<String>,
        goalState: List<String>
    ): Result {
        val startNode = Node(startState, null, null, 0, 0)
        val fringe = mutableListOf<Node>()
        val path = mutableListOf<String>()
        fringe.add(startNode)
        var currentNode = fringe.removeFirst()
        var expandedNode = 0
        while (currentNode.state != goalState) {
            expandedNode++
            val temp = expandNode(currentNode)
            for (node in temp) {
                node.depth += currentNode.depth
                fringe.add(node)
            }
            fringe.sortBy { it.depth }
            currentNode = fringe.removeFirst()
            Log.i(
                TAG, "remove " + currentNode.state.toString() + " ${currentNode.operator}"
            )
        }
        val depth = currentNode.depth
        while (currentNode.parent != null) {
            path.add(0, currentNode.operator!!)
            currentNode = currentNode.parent!!
        }
        return Result(
            path,
            path.size.toString(),
            depth.toString(),
            depth.toString(),
            expandedNode.toString()
        )
    }

    private fun iterativeDeepeningSearch(
        startState: MutableList<String>,
        goalState: List<String>,
    ): Result {
        val fringeStack = Stack<Node>()
        val path = mutableListOf<String>()
        var currentNode = Node(startState, null, null, 0, 0)
        var i = 1
        var expandedNode = 0
        while (currentNode.state != goalState) {
            val startNode = Node(startState, null, null, 0, 0)
            path.clear()
            fringeStack.clear()
            fringeStack.push(startNode)
            currentNode = fringeStack.pop()
            while (currentNode.state != goalState) {
                expandedNode++
                val temp = expandNode(currentNode)
                for (node in temp) {
                    if (currentNode.depth > i)
                        continue
                    fringeStack.push(node)
                }
                if (fringeStack.isEmpty())
                    break
                currentNode = fringeStack.pop()
            }
            i++
        }
        val depth = currentNode.depth
        while (currentNode.parent != null) {
            path.add(0, currentNode.operator!!)
            currentNode = currentNode.parent!!
        }
        return Result(
            path,
            path.size.toString(),
            depth.toString(),
            depth.toString(),
            expandedNode.toString()
        )
    }

    private fun aStarSearch(
        startState: MutableList<String>,
        goalState: List<String>,
        heuristicType: Int
    ): Result {
        val startNode = Node(startState, null, null, 0, 0)
        val fringe = mutableListOf<Node>()
        val path = mutableListOf<String>()
        fringe.add(startNode)
        var currentNode = fringe.removeFirst()
        var expandedNode = 0
        while (currentNode.state != goalState) {
            expandedNode++
            val temp = expandNode(currentNode)
            for (node in temp) {
                val h: Int = when (heuristicType) {
                    1 -> heuristic1(node, goalState)
                    2 -> heuristic2(node)
                    else -> {
                        1
                    }
                }
                node.heuristic = h + node.depth
                fringe.add(node)
            }
            fringe.sortBy { it.heuristic }
            currentNode = fringe.removeFirst()
            Log.i(
                TAG, "remove " + currentNode.state.toString() + " ${currentNode.operator}"
            )
        }
        val depth = currentNode.depth
        while (currentNode.parent != null) {
            path.add(0, currentNode.operator!!)
            currentNode = currentNode.parent!!
        }
        return Result(
            path,
            path.size.toString(),
            depth.toString(),
            depth.toString(),
            expandedNode.toString()
        )
    }

    private fun iterativeDeepeningAStarSearch(
        startState: MutableList<String>,
        goalState: List<String>,
        heuristicType: Int
    ): Result {
        val fringeStack = Stack<Node>()
        val path = mutableListOf<String>()
        var currentNode = Node(startState, null, null, 0, 0)
        var i = 1
        var expandedNode = 0
        while (currentNode.state != goalState) {
            val startNode = Node(startState, null, null, 0, 0)
            path.clear()
            fringeStack.clear()
            fringeStack.push(startNode)
            currentNode = fringeStack.pop()
            while (currentNode.state != goalState) {
                expandedNode++
                val temp = expandNode(currentNode)
                for (node in temp) {
                    val h: Int = when (heuristicType) {
                        1 -> heuristic1(node, goalState)
                        2 -> heuristic2(node)
                        else -> {
                            1
                        }
                    }
                    node.heuristic = h + node.depth
                    if (currentNode.heuristic != null && currentNode.heuristic!! > i)
                        continue
                    fringeStack.push(node)
                }
                if (fringeStack.isEmpty())
                    break
                currentNode = fringeStack.pop()
            }
            i++
        }
        val depth = currentNode.depth
        while (currentNode.parent != null) {
            path.add(0, currentNode.operator!!)
            currentNode = currentNode.parent!!
        }
        return Result(
            path,
            path.size.toString(),
            depth.toString(),
            depth.toString(),
            expandedNode.toString()
        )
    }

    private fun isSolvable(startState: MutableList<String>): Boolean {
        var inversionCount = 0
        val newList = startState.toMutableList()
        newList.remove(" ")
        for (i in 0..7) {
            for (j in 0..i) {
                if (newList[j] > newList[i])
                    inversionCount++
            }
        }
        return (inversionCount % 2 == 0)
    }

    private fun heuristic1(node: Node, goalState: List<String>): Int {
        var heuristic = 0
        for (i in 0..8) {
            if (node.state[i] != goalState[i])
                heuristic += 1
        }
        return heuristic
    }

    private fun heuristic2(node: Node): Int {
        var heuristic = 0

        if (node.state[0] != " ")
            heuristic += heuristic2Check(node.state[0], 1, 1)
        if (node.state[1] != "1")
            heuristic += heuristic2Check(node.state[1], 1, 2)
        if (node.state[2] != "2")
            heuristic += heuristic2Check(node.state[2], 1, 3)
        if (node.state[3] != "3")
            heuristic += heuristic2Check(node.state[3], 2, 1)
        if (node.state[4] != "4")
            heuristic += heuristic2Check(node.state[4], 2, 2)
        if (node.state[5] != "5")
            heuristic += heuristic2Check(node.state[5], 2, 3)
        if (node.state[6] != "6")
            heuristic += heuristic2Check(node.state[6], 3, 1)
        if (node.state[7] != "7")
            heuristic += heuristic2Check(node.state[7], 3, 2)
        if (node.state[8] != "8")
            heuristic += heuristic2Check(node.state[8], 3, 3)

        return heuristic
    }

    private fun heuristic2Check(tile: String, row: Int, column: Int): Int {
        return when (tile) {
            " " -> abs((row - 1)) + abs((column - 1))
            "1" -> abs((row - 1)) + abs((column - 2))
            "2" -> abs((row - 1)) + abs((column - 3))
            "3" -> abs((row - 2)) + abs((column - 1))
            "4" -> abs((row - 2)) + abs((column - 2))
            "5" -> abs((row - 2)) + abs((column - 3))
            "6" -> abs((row - 3)) + abs((column - 1))
            "7" -> abs((row - 3)) + abs((column - 2))
            "8" -> abs((row - 3)) + abs((column - 3))
            else -> {
                0
            }
        }
    }
}