package com.mwdch.eight_puzzlesolution

data class Node(
    var state: MutableList<String>,
    var parent: Node?,
    var operator: String?,
    var depth: Int,
    var cost: Int,
    var heuristic: Int? = null
)
