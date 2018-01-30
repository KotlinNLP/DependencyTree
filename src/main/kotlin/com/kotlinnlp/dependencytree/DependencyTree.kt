/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.dependencytree

import java.util.*
import kotlin.collections.ArrayList

/**
 * [DependencyTree] contains convenient methods to build and navigate a dependency tree.
 *
 * @property size
 */
class DependencyTree(val size: Int) {

  companion object {

    /**
     * Initialize the DependencyTree with a given list of [dependencies].
     *
     * @param dependencies a list of [DependencyConfiguration]
     *
     * @return a new DependencyTree
     */
    operator fun invoke(size: Int, dependencies: List<DependencyConfiguration>): DependencyTree {

      val tree = DependencyTree(size)

      dependencies.forEach {
        when  {
          it is RootConfiguration && it.deprel != null -> tree.setDeprel(dependent = it.id, deprel = it.deprel!!)
          it is ArcConfiguration -> tree.setArc(dependent = it.dependent, governor = it.governor, deprel = it.deprel)
        }
      }

      return tree
    }
  }

  /**
   * List of the elements of the tree, in linear order.
   */
  val elements = IntRange(start = 0, endInclusive = this.size - 1)

  /**
   * List of heads, one per element.
   * Not assigned elements have null head.
   */
  val heads = arrayOfNulls<Int>(size = this.size)

  /**
   * List of deprels, one per element.
   * Not assigned elements have null deprel.
   */
  val deprels = arrayOfNulls<Deprel>(size = this.size)

  /**
   * List of POS tags, one per element.
   * Not assigned elements have null pos.
   */
  val posTags = arrayOfNulls<POSTag>(size = this.size)

  /**
   * The list of left dependents for each element. In case of no dependents the list is empty.
   */
  val leftDependents = Array(size = this.size, init = { ArrayList<Int>() } )

  /**
   * The list of right dependents for each element. In case of no dependents the list is empty.
   */
  val rightDependents = Array(size = this.size, init = { ArrayList<Int>() } )

  /**
   * List of root elements.
   */
  val roots = ArrayList(elements.toList())

  /**
   * @param element an element of the tree
   *
   * @return the list of dependents of the [element] (both left and right)
   */
  fun dependentsOf(element: Int): List<Int> = this.leftDependents[element].plus(this.rightDependents[element])

  /**
   * @return a Boolean indicating whether the tree is single root
   */
  fun hasSingleRoot(): Boolean = this.roots.size == 1

  /**
   * @param element an element of the tree
   *
   * @return a Boolean indicating whether the [element] isn't in the [roots]
   */
  fun isAssigned(element: Int): Boolean = element !in this.roots

  /**
   * @param element an element of the tree
   *
   * @return a Boolean indicating whether the [element] is in the [roots]
   */
  fun isNotAssigned(element: Int): Boolean = element in this.roots

  /**
   * Set a new arc between a given [dependent] and [governor], possibly with a [deprel] and a [posTag].
   *
   * @param dependent an element of the tree
   * @param governor an element of the tree
   * @param deprel a deprel (can be null)
   * @param posTag a posTag (can be null)
   */
  fun setArc(dependent: Int, governor: Int, deprel: Deprel? = null, posTag: POSTag? = null) {

    require(governor in 0 until this.size) {
      "Governor [$governor] out of range 0 .. ${this.elements.last}"
    }

    require(dependent in 0 until this.size) {
      "Dependent [$dependent] out of range 0 .. ${this.elements.last}"
    }

    require(this.heads[dependent] == null) {
      "Dependent has already an head"
    }

    this.roots.remove(dependent)
    this.heads[dependent] = governor
    this.deprels[dependent] = deprel
    this.posTags[dependent] = posTag
    this.addDependent(dependent, governor = governor)
  }

  /**
   * Add the given [dependent] to the [leftDependents] or [rightDependents] of the given [governor].
   *
   * @param dependent an element of the tree
   * @param governor an element of the tree
   */
  fun addDependent(dependent: Int, governor: Int) {
    if (dependent < governor) {
      this.leftDependents[governor].add(dependent)
    } else {
      this.rightDependents[governor].add(dependent)
    }
  }

  /**
   * Set a [deprel] to the given [dependent].
   *
   * @param dependent an element of the tree
   * @param deprel a Deprel
   */
  fun setDeprel(dependent: Int, deprel: Deprel){

    this.deprels[dependent] = deprel
  }

  /**
   * Set a [posTag] to the given [dependent].
   *
   * @param dependent an element of the tree
   * @param posTag a POS tag
   */
  fun setPosTag(dependent: Int, posTag: POSTag){

    this.posTags[dependent] = posTag
  }

  /**
   * Iterate over all the ancestors of a given [element], calling the [callback] for each of them.
   *
   * @param element an element of the tree
   * @param callback a function that receives an element as argument
   */
  fun forEachAncestor(element: Int, callback: ((Int) -> Unit)? = null) {

    var head: Int? = this@DependencyTree.heads[element]
    var firstHead: Int? = null

    while (head != null && head != firstHead) {

      if (firstHead == null) firstHead = head

      callback?.invoke(head)

      head = this@DependencyTree.heads[head]
    }
  }

  /**
   * Iterate over all the ancestors of a given [element], calling the [callback] for each of them.
   * If the [element] is involved in a cycle, the loop ends with the element itself.
   *
   * @param element an element of the tree
   * @param callback a function that receives an element as argument and returns a boolean
   *
   * @return true if any call of the [callback] returned true, otherwise false
   */
  fun anyAncestor(element: Int, callback: (Int?) -> Boolean): Boolean {

    var head: Int? = this@DependencyTree.heads[element]
    var firstHead: Int? = null

    while (head != null && head != firstHead) {

      if (firstHead == null) firstHead = head

      if (callback(head)) return true

      head = this@DependencyTree.heads[head]
    }

    return false
  }

  /**
   * Check if the tree contains cycle.
   *
   * Implementation note: in an acyclic tree, the path from each word following the head relation upwards always ends
   * at the root node.
   *
   * @return a Boolean indicating whether the tree contains cycles
   */
  fun containsCycle(): Boolean {

    this.elements.forEach { index ->

      try {
        this.forEachAncestor(index)

      } catch (e: CycleDetectedError) {
        return true
      }
    }

    return false
  }

  /**
   * Check if an arc between two elements introduces a cycle.
   *
   * @param dependent the dependent element of the arc
   * @param governor the governor element of the arc
   *
   * @return a Boolean indicating whether the arc between the given [dependent] and [governor] introduces a cycle
   */
  fun checkCycleWith(dependent: Int, governor: Int)
    = dependent == governor || this.isAncestorOf(candidateAncestor = dependent, element = governor)

  /**
   * Whether a dependent element is involved in a non-projective arc.
   *
   * An arc h -> d | h < d is projective if all the elements k | h < k < d are descendants of h.
   * Analogue for h -> d, h > d. Otherwise the Arc is defined non-projective.
   *
   * @param dependent a dependent node of the tree.
   *
   * @return a Boolean indicating whether the given [dependent] is involved in a non-projective arc
   */
  fun inNonProjectiveArc(dependent: Int): Boolean {

    val head = this.heads[dependent]

    return if (head == null) {

      false

    } else {

      val middleElements = IntRange(minOf(head, dependent) + 1, maxOf(head, dependent) - 1)

      // optimized with negated condition
      middleElements.any { !this.isAncestorOf(candidateAncestor = head, element = it) }
    }
  }

  /**
   * @param candidateAncestor an element of the tree
   * @param element an element of the tree
   *
   * @return a Boolean indicating whether the given [candidateAncestor] is the ancestor of the given [element]
   */
  fun isAncestorOf(candidateAncestor: Int, element: Int) = this.anyAncestor(element) { it == candidateAncestor }

  /**
   * @param element an element of the tree
   *
   * @return a Boolean indicating whether the given [element] is a root
   */
  fun isRoot(element: Int) = element in this.roots

  /**
   * @return a Boolean indicating whether the tree contains at least one non-projective arc
   */
  fun isNonProjectiveTree(): Boolean = this.elements.any { this.inNonProjectiveArc(it) }

  /**
   * @return a Boolean indicating whether the forest contains a single directed acyclic graph
   */
  fun isTree(): Boolean = this.hasSingleRoot() && !this.containsCycle()

  /**
   * The projective order is a canonical (re)ordering of the elements for which the tree is projective.
   *
   * Implementation note: the projective order is obtained through an inorder traversal of the tree that respects the
   * local order of a head and its dependents.
   *
   * @return a list of elements in the projective order
   */
  fun inOrder(): List<Int> {

    fun helper(i: Int): List<Int> {

      val results = ArrayList<Int>()

      (0 .. i).filter { this.heads[it] == i }.forEach { results.addAll(helper(it)) }

      results.add(i)

      (i until this.size).filter { this.heads[it] == i }.forEach { results.addAll(helper(it)) }

      return results
    }

    require(this.isTree()) { "Required a single directed acyclic graph."}

    return helper(this.roots.first())
  }

  /**
   * @return an array where the i-th value is the projective order of the i-th element
   */
  fun projectiveOrder(): List<Int> {

    val results = arrayOfNulls<Int>(size = this.size)

    this.inOrder().mapIndexed { index, element -> results[element] = index }

    return results.requireNoNulls().toList()
  }

  /**
   * @param deprels a list of [Deprel]s
   *
   * @return a Boolean indicating whether the given [deprels] matches the deprel of this [DependencyTree]
   */
  fun matchDeprels(deprels: Array<Deprel?>): Boolean = Arrays.equals(this.deprels, deprels)

  /**
   * @return a Boolean indicating whether the given [heads] matches the heads of this [DependencyTree]
   */
  fun matchHeads(heads: Array<Int?>): Boolean = Arrays.equals(this.heads, heads)

  /**
   * @return a string representation of the [elements] and their dependencies
   */
  override fun toString(): String = PrintHelper(this).print()

  /**
   * @return a string representation of the [elements] and their dependencies
   */
  fun toString(words: List<String>): String = PrintHelper(tree = this, words = words).print()

  /**
   * @return a copy of this [DependencyTree]
   */
  fun clone(): DependencyTree {

    val tree = DependencyTree(this.size)

    tree.roots.clear()
    tree.roots.addAll(this.roots)

    this.heads.forEachIndexed { index, head -> tree.heads[index] = head }
    this.deprels.forEachIndexed { index, deprel -> tree.deprels[index] = deprel }
    this.posTags.forEachIndexed { index, posTag -> tree.posTags[index] = posTag }
    this.leftDependents.forEachIndexed { index, dependent -> tree.leftDependents[index] = dependent }
    this.rightDependents.forEachIndexed { index, dependent -> tree.rightDependents[index] = dependent }

    return tree
  }

  /**
   * @return a Boolean indicating whether the given [other] object is equal to this [DependencyTree]
   */
  override operator fun equals(other: Any?): Boolean
    = other is DependencyTree && this.matchHeads(other.heads) && this.matchDeprels(other.deprels)

  /**
   * @return the hash code of this [DependencyTree]
   */
  override fun hashCode(): Int = this.heads.hashCode() * 8191 + this.deprels.hashCode()
}
