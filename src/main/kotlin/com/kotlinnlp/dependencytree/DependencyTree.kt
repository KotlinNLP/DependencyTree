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
     * @param size the number of elements in the tree
     * @param dependencies a list of [DependencyConfiguration]
     * @param allowCycles if true it allows to create cycles when building the tree (default = false)
     *
     * @return a new DependencyTree
     */
    operator fun invoke(size: Int,
                        dependencies: List<DependencyConfiguration>,
                        allowCycles: Boolean = false): DependencyTree {

      val tree = DependencyTree(size)

      dependencies.forEach {
        when  {

          it is RootConfiguration && it.deprel != null -> tree.setDeprel(
            dependent = it.id,
            deprel = it.deprel!!)

          it is ArcConfiguration -> tree.setArc(
            dependent = it.dependent,
            governor = it.governor,
            deprel = it.deprel,
            score = it.attachmentScore,
            allowCycle = allowCycles)
        }
      }

      return tree
    }
  }

  /**
   * An arc between two elements.
   *
   * @property dependent the dependent element
   * @property governor the governor element
   */
  data class Arc(val dependent: Int, val governor: Int)

  /**
   * A path that link more arcs.
   *
   * @property arcs the list of arcs of this path
   */
  data class Path(val arcs: List<Arc>)

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
   * List of attachment scores, one for each dependent element (also roots can have it), default = 0.0.
   */
  val attachmentScores = Array(size = this.size, init = { 0.0 })

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
   * Set a new arc between a given [dependent] and [governor], possibly with a [deprel] and a [posTag].
   *
   * @param dependent an element of the tree
   * @param governor an element of the tree
   * @param deprel a deprel (can be null)
   * @param posTag a posTag (can be null)
   * @param score the attachment score (default = 0.0)
   * @param allowCycle if true it allows to create cycles setting this arc (default = false)
   *
   * @throws CycleDetectedError if [allowCycle] is false and this arc introduces a cycle
   */
  fun setArc(dependent: Int,
             governor: Int,
             deprel: Deprel? = null,
             posTag: POSTag? = null,
             score: Double = 0.0,
             allowCycle: Boolean = false) {

    require(governor in 0 until this.size) { "Governor [$governor] out of range 0 .. ${this.elements.last}" }
    require(dependent in 0 until this.size) { "Dependent [$dependent] out of range 0 .. ${this.elements.last}" }
    require(this.heads[dependent] == null) { "Dependent has already an head" }

    if (!allowCycle && this.introduceCycle(dependent = dependent, governor = governor)) throw CycleDetectedError()

    this.roots.remove(dependent)
    this.heads[dependent] = governor
    this.deprels[dependent] = deprel
    this.posTags[dependent] = posTag
    this.attachmentScores[dependent] = score
    this.addDependent(dependent = dependent, governor = governor)
  }

  /**
   * Remove the arc between the given [dependent] and [governor].
   *
   * @param dependent an element of the tree
   * @param governor an element of the tree
   *
   * @throws InvalidArc if the given [dependent] and [governor] are not involved in an arc
   */
  fun removeArc(dependent: Int, governor: Int) {

    require(governor in 0 until this.size) { "Governor [$governor] out of range 0 .. ${this.elements.last}" }
    require(dependent in 0 until this.size) { "Dependent [$dependent] out of range 0 .. ${this.elements.last}" }

    if (this.heads[dependent] != governor) throw InvalidArc(dependent = dependent, governor = governor)

    this.addRoot(dependent)
    this.heads[dependent] = null
    this.deprels[dependent] = null
    this.posTags[dependent] = null
    this.attachmentScores[dependent] = 0.0
    this.removeDependent(dependent = dependent, governor = governor)
  }

  /**
   * Set a [deprel] to the given [dependent].
   *
   * @param dependent an element of the tree
   * @param deprel a Deprel
   */
  fun setDeprel(dependent: Int, deprel: Deprel) {
    this.deprels[dependent] = deprel
  }

  /**
   * Set a [posTag] to the given [dependent].
   *
   * @param dependent an element of the tree
   * @param posTag a POS tag
   */
  fun setPosTag(dependent: Int, posTag: POSTag) {
    this.posTags[dependent] = posTag
  }

  /**
   * Set an attachment to the given [dependent].
   *
   * It is required that the given [dependent] has an head assigned.
   *
   * @param dependent an element of the tree
   * @param score the score to assign
   */
  fun setAttachmentScore(dependent: Int, score: Double) {
    this.attachmentScores[dependent] = score
  }

  /**
   * Iterate over all the ancestors of a given [element], calling the [callback] for each of them.
   *
   * @param element an element of the tree
   * @param callback a function that receives an element as argument
   */
  fun forEachAncestor(element: Int, callback: ((Int) -> Unit)? = null) {

    var head: Int? = this@DependencyTree.heads[element]
    val visited = mutableSetOf<Int>()

    while (head != null && head !in visited) {

      callback?.invoke(head)

      visited.add(head)

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
    val visited = mutableSetOf<Int>()

    while (head != null && head !in visited) {

      if (callback(head)) return true

      visited.add(head)

      head = this@DependencyTree.heads[head]
    }

    return false
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
  fun isRoot(element: Int) = this.heads[element] == null

  /**
   * Whether an element has a head.
   *
   * @param element an element of the tree
   *
   * @return a Boolean indicating whether the [element] has a head
   */
  fun isAssigned(element: Int): Boolean = !this.isRoot(element)

  /**
   * @param element an element of the tree
   *
   * @return a Boolean indicating whether the [element] has not a head
   */
  fun isNotAssigned(element: Int): Boolean = this.isRoot(element)

  /**
   * Check if the tree contains cycles.
   *
   * @return a Boolean indicating whether the tree contains cycles
   */
  fun containsCycle(): Boolean {

    val visited = mutableSetOf<Int>()

    this.elements.forEach { elm ->

      if (elm !in visited) {

        var head: Int? = this.heads[elm]
        val visitedHeads = mutableSetOf<Int>()

        while (head != null) {

          if (head in visitedHeads) return true

          visitedHeads.add(head)

          head = this.heads[head]
        }

        visited.add(elm)
        visited.addAll(visitedHeads)
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
  fun introduceCycle(dependent: Int, governor: Int)
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
  fun isNonProjectiveArc(dependent: Int): Boolean {

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
   * Whether this tree is non-projective.
   *
   * @return a Boolean indicating whether this tree contains at least one non-projective arc
   */
  fun isNonProjective(): Boolean = this.elements.any { this.isNonProjectiveArc(it) }

  /**
   * Whether this tree is projective.
   *
   * @return a Boolean indicating whether this tree contains all projective arcs
   */
  fun isProjective(): Boolean = !this.isNonProjective()

  /**
   * Whether this tree is a fully-connected directed acyclic graph (DAG).
   *
   * @return a Boolean indicating if this tree is a fully-connected directed acyclic graph
   */
  fun isDAG(): Boolean = this.hasSingleRoot() && !this.containsCycle()

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

    require(this.isDAG()) { "Required a single directed acyclic graph."}

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
   * Get a list of paths that represent the cycles of this tree.
   *
   * @return the list of cycles
   */
  fun getCycles(): List<Path> {

    val cycles = mutableListOf<Path>()
    val visited = mutableSetOf<Int>()

    this.elements.forEach { elm ->

      if (elm !in visited) {

        var head: Int? = this.heads[elm]

        if (head != null) {

          val visitedHeads = mutableSetOf(elm)

          while (head != null && head !in visited && head !in visitedHeads) {

            visitedHeads.add(head)

            head = this.heads[head]
          }

          if (head != null && head in visitedHeads) {
            cycles.add(this.getCycle(startElement = head))
          }

          visited.addAll(visitedHeads)

        } else {
          visited.add(elm)
        }
      }
    }

    return cycles.toList()
  }

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
    this.attachmentScores.forEachIndexed { index, score -> tree.attachmentScores[index] = score }
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

  /**
   * Add the given [dependent] to the [leftDependents] or [rightDependents] of the given [governor].
   *
   * @param dependent an element of the tree
   * @param governor an element of the tree
   */
  private fun addDependent(dependent: Int, governor: Int) {
    if (dependent < governor) {
      this.leftDependents[governor].add(dependent)
    } else {
      this.rightDependents[governor].add(dependent)
    }
  }

  /**
   * Add the given element as root.
   *
   * @param element an element of the tree
   */
  private fun addRoot(element: Int) {

    for (i in (0 until this.roots.size)) {
      val root: Int = this.roots[i]

      if (root > element) {
        this.roots.add(i, element)
        return
      }
    }

    this.roots.add(element)
  }

  /**
   * Remove the given [dependent] from the [leftDependents] or [rightDependents] of the given [governor].
   *
   * @param dependent an element of the tree
   * @param governor an element of the tree
   */
  private fun removeDependent(dependent: Int, governor: Int) {
    if (dependent < governor) {
      this.leftDependents[governor].remove(dependent)
    } else {
      this.rightDependents[governor].remove(dependent)
    }
  }

  /**
   * @param startElement the start element of a cycle
   *
   * @return the cycle path built starting from [startElement]
   */
  private fun getCycle(startElement: Int): Path {

    val cycle = mutableListOf<Arc>()
    var head: Int = this.heads[startElement]!!

    cycle.add(Arc(dependent = startElement, governor = head))

    while (head != startElement) {

      val nextHead: Int = this.heads[head]!!

      cycle.add(Arc(dependent = head, governor = nextHead))

      head = nextHead
    }

    return Path(cycle)
  }
}
