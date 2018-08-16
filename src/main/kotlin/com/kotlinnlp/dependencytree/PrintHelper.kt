/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.dependencytree

/**
 * The print helper of a [DependencyTree].
 *
 * @param tree the dependency tree to print
 * @param words a list of words to associate to each element of the given [tree] (if null the ids are used)
 */
internal class PrintHelper(private val tree: DependencyTree, private val words: List<String>? = null) {

  /**
   * Enum class that defines the position of an element respect to its parent.
   *
   * @property ROOT the position of a root element
   * @property LEFT the position of a left dependent element
   * @property RIGHT the position of a right dependent element
   */
  private enum class Position { ROOT, LEFT, RIGHT }

  /**
   * The string used as padding of an element that is a left dependent.
   */
  private val leftDepStrPadding = "+--l "

  /**
   * The string used as padding of an element that is a right dependent.
   */
  private val rightDepStrPadding = "+--r "

  /**
   * The string used as padding of a level in which the related element is a leaf.
   */
  private val emptyPadding = "     "

  /**
   * The string used as padding of a level in which the related element is not a leaf.
   */
  private val continuePadding = "|    "

  /**
   * The keyword used as null POS tag or deprel.
   */
  private val nullKeyword = "_"

  /**
   * Check the number of words.
   */
  init {
    require(this.words == null || this.words.size == tree.size)
  }

  /**
   * @return the string representation of the [tree]
   */
  fun print(): String {

    return this.tree.roots
      .joinToString(separator = "\n") {
        this.subTreeToString(element = it, position = Position.ROOT, ancestorsAreLeaves = listOf())
      }
      .trim()
  }

  /**
   * @param element an element of the [tree], considered as root of a sub-tree
   * @param position the position of the [element]
   * @param ancestorsAreLeaves an array of booleans indicating if each ancestor is a leaf (excluding the root)
   *
   * @return the string representation of the sub-tree with the given [element] as root
   */
  private fun subTreeToString(element: Int,
                              position: Position,
                              ancestorsAreLeaves: List<Boolean>): String {

    val buf = StringBuffer()

    buf.append(this.getElementPadding(position = position, ancestorsAreLeaves = ancestorsAreLeaves))
    buf.append(this.getElementString(element))
    buf.append("\n")
    buf.append(this.descendantsToString(element = element, ancestorsAreLeaves = ancestorsAreLeaves))

    return buf.toString()
  }

  /**
   * @param position the position of an element of the [tree]
   * @param ancestorsAreLeaves an array of booleans indicating if each ancestor is a leaf (excluding the root)
   *
   * @return the string padding of an element of the [tree]
   */
  private fun getElementPadding(position: Position, ancestorsAreLeaves: List<Boolean>): String {

    val buf = StringBuffer()

    if (position != Position.ROOT) {

      ancestorsAreLeaves
        .subList(0, ancestorsAreLeaves.lastIndex)
        .forEach { buf.append(if (it) this.emptyPadding else this.continuePadding) }

      buf.append(if (position == Position.LEFT) this.leftDepStrPadding else this.rightDepStrPadding)
    }

    return buf.toString()
  }

  /**
   * Get the string representing a single element of the [tree], with its deprel and POS tag.
   *
   * @param element an element of the [tree]
   *
   * @return the string representing an element of the [tree]
   */
  private fun getElementString(element: Int): String {

    val elementIndex: Int = this.tree.getPosition(element)

    val name: String = this.words?.get(elementIndex) ?: element.toString()
    val posTag = this.tree.getPosTag(element) ?: this.nullKeyword
    val deprel = this.tree.getDeprel(element) ?: this.nullKeyword

    return "$name $posTag $deprel"
  }

  /**
   * @param element an element of the [tree]
   * @param ancestorsAreLeaves an array of booleans indicating if each ancestor is a leaf (excluding the root)
   *
   * @return the string representation of all the descendants of the given [element]
   */
  private fun descendantsToString(element: Int, ancestorsAreLeaves: List<Boolean>): String {

    val leftDependents: List<Int> = this.tree.getLeftDependents(element)
    val rightDependents: List<Int> = this.tree.getRightDependents(element)
    val childrenTotal = leftDependents.size + rightDependents.size

    val buf = StringBuffer()
    var childrenCount = 0

    leftDependents.forEach {
      buf.append(
        this.subTreeToString(
          element = it,
          position = Position.LEFT,
          ancestorsAreLeaves = ancestorsAreLeaves + listOf(++childrenCount == childrenTotal)
        )
      )
    }

    rightDependents.forEach {
      buf.append(
        this.subTreeToString(
          element = it,
          position = Position.RIGHT,
          ancestorsAreLeaves = ancestorsAreLeaves + listOf(++childrenCount == childrenTotal)
        )
      )
    }

    return buf.toString()
  }
}
