/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.dependencytree

import com.kotlinnlp.conllio.Sentence
import com.kotlinnlp.conllio.Token
import com.kotlinnlp.dependencytree.configuration.ArcConfiguration
import com.kotlinnlp.dependencytree.configuration.DependencyConfiguration
import com.kotlinnlp.dependencytree.configuration.RootConfiguration
import com.kotlinnlp.linguisticdescription.DependencyRelation
import com.kotlinnlp.linguisticdescription.Deprel

/**
 * [DependencyTree] contains methods to build and navigate a dependency tree.
 *
 * @property elements the list of the elements in the dependency tree (each represented by an integer ID)
 */
class DependencyTree(val elements: List<Int>) {

  companion object {

    /**
     * Build a DependencyTree with a given list of [dependencies].
     *
     * @param elements the list of the ids of the elements in the dependency tree
     * @param dependencies a list of [DependencyConfiguration]
     * @param allowCycles if true it allows to create cycles when building the tree (default = false)
     *
     * @return a new DependencyTree
     */
    operator fun invoke(elements: List<Int>,
                        dependencies: List<DependencyConfiguration>,
                        allowCycles: Boolean = false): DependencyTree {

      val tree = DependencyTree(elements)

      dependencies.forEach {
        when  {
          it is RootConfiguration && it.dependencyRelation != null -> tree.setDependencyRelation(
            dependent = it.id,
            dependencyRelation = it.dependencyRelation!!)
          it is ArcConfiguration -> tree.setArc(
            dependent = it.dependent,
            governor = it.governor,
            dependencyRelation = it.dependencyRelation,
            score = it.attachmentScore,
            allowCycle = allowCycles)
        }
      }

      return tree
    }

    /**
     * Build a DependencyTree with a size.
     * It will contain elements with sequential ids, from 0 to (size - 1).
     *
     * @param size the number of elements in the dependency tree
     * @param dependencies a list of [DependencyConfiguration]
     * @param allowCycles if true it allows to create cycles when building the tree (default = false)
     *
     * @return a new DependencyTree
     */
    operator fun invoke(size: Int,
                        dependencies: List<DependencyConfiguration>,
                        allowCycles: Boolean = false): DependencyTree = this(
      elements = IntRange(0, size - 1).toList(),
      dependencies = dependencies,
      allowCycles = allowCycles)

    /**
     * Build a [DependencyTree] from a CoNLL sentence.
     *
     * @param sentence a CoNLL sentence
     *
     * @return a new dependency tree defined in the given sentence
     */
    operator fun invoke(sentence: Sentence): DependencyTree {

      val tree = DependencyTree(elements = sentence.tokens.map { it.id })

      sentence.tokens.forEach { tree.setArc(it) }

      return tree
    }
  }

  /**
   * Build a DependencyTree with a size.
   * It will contain elements with sequential ids, from 0 to (size - 1).
   *
   * @param size the number of elements in the dependency tree
   *
   * @return a new DependencyTree
   */
  constructor(size: Int): this(IntRange(0, size - 1).toList())

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
   * The number of elements into the tree.
   */
  val size: Int = this.elements.size

  /**
   * The confidence score of the tree.
   */
  var score = 1.0

  /**
   * A map of elements to their ordinal position.
   */
  private val elementsToPosition: Map<Int, Int> = (0 until this.size).associate { this.elements[it] to it }

  /**
   * The map of elements to their heads.
   * Not assigned elements have null head.
   */
  internal val heads: MutableMap<Int, Int?> = this.elements.associate { it to null }.toMutableMap()

  /**
   * The map of elements to their dependency relations.
   * Not assigned elements have null dependency relation.
   */
  internal val dependencyRelations: MutableMap<Int, DependencyRelation?> =
    this.elements.associate { it to null }.toMutableMap()

  /**
   * The map of elements to their attachment scores (roots included, default = 0.0).
   */
  internal val attachmentScores: MutableMap<Int, Double> = this.elements.associate { it to 0.0 }.toMutableMap()

  /**
   * The map of elements to their left dependents.
   * In case of no dependents the list is empty.
   */
  private val leftDependents: Map<Int, MutableList<Int>> = this.elements.associate { it to mutableListOf<Int>() }

  /**
   * The map of elements to their right dependents.
   * In case of no dependents the list is empty.
   */
  private val rightDependents: Map<Int, MutableList<Int>> = this.elements.associate { it to mutableListOf<Int>() }

  /**
   * List of root elements.
   */
  internal val roots: MutableList<Int> = this.elements.toMutableList()

  /**
   * The set of elements.
   */
  private val elementsSet = this.elements.toSet()

  /**
   * @param element an element of the tree
   *
   * @return the head of the given element
   */
  fun getHead(element: Int): Int? = this.heads.getValue(element)

  /**
   * @param element an element of the tree
   *
   * @return the dependency relation of the given element
   */
  fun getDependencyRelation(element: Int): DependencyRelation? = this.dependencyRelations.getValue(element)

  /**
   * @param element an element of the tree
   *
   * @return the attachment score of the given element
   */
  fun getAttachmentScore(element: Int): Double = this.attachmentScores.getValue(element)

  /**
   * @param element an element of the tree
   *
   * @return the list of left dependents of the given element
   */
  fun getLeftDependents(element: Int): List<Int> = this.leftDependents.getValue(element)

  /**
   * @param element an element of the tree
   *
   * @return the list of right dependents of the given element
   */
  fun getRightDependents(element: Int): List<Int> = this.rightDependents.getValue(element)

  /**
   * @param element an element of the tree
   *
   * @return the list of all the dependents (left + right) of the given element
   */
  fun getDependents(element: Int): List<Int> = this.getLeftDependents(element) + this.getRightDependents(element)

  /**
   * @param element an element of the tree
   *
   * @return the list of all the descendants (dependents at any depth) of the given element
   */
  fun getAllDescendants(element: Int): List<Int> = this.getDependents(element).flatMap { this.getAllDescendants(it) }

  /**
   * @return the list of roots of the tree
   */
  fun getRoots(): List<Int> = this.roots

  /**
   * @param element an element of the tree
   *
   * @return the ordinal position of the element
   */
  fun getPosition(element: Int): Int = this.elementsToPosition.getValue(element)

  /**
   * @return a Boolean indicating whether the tree has a single root
   */
  fun hasSingleRoot(): Boolean = this.roots.size == 1

  /**
   * Set a new arc between a given dependent and governor, possibly with a dependency relation.
   *
   * @param dependent an element of the tree
   * @param governor an element of the tree
   * @param dependencyRelation a dependency relation (can be null)
   * @param score the attachment score (default = 0.0)
   * @param allowCycle if true it allows to create cycles setting this arc (default = false)
   *
   * @throws CycleDetectedError if [allowCycle] is false and this arc introduces a cycle
   */
  fun setArc(dependent: Int,
             governor: Int,
             dependencyRelation: DependencyRelation? = null,
             score: Double = 0.0,
             allowCycle: Boolean = false) {

    require(governor in this.elementsSet) { "Invalid governor: $governor" }
    require(dependent in this.elementsSet) { "Invalid dependent: $dependent" }
    require(this.heads[dependent] == null) { "Dependent has already an head" }

    if (!allowCycle && this.introduceCycle(dependent = dependent, governor = governor)) throw CycleDetectedError()

    this.roots.remove(dependent)
    this.heads[dependent] = governor
    this.dependencyRelations[dependent] = dependencyRelation
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

    require(governor in this.elementsSet) { "Invalid governor: $governor" }
    require(dependent in this.elementsSet) { "Invalid dependent: $dependent" }

    if (this.getHead(dependent) != governor) throw InvalidArc(dependent = dependent, governor = governor)

    this.setRoot(dependent)
    this.heads[dependent] = null
    this.dependencyRelations[dependent] = null
    this.attachmentScores[dependent] = 0.0
    this.removeDependent(dependent = dependent, governor = governor)
  }

  /**
   * Set a dependency relation to a given dependent.
   *
   * @param dependent an element of the tree
   * @param dependencyRelation a dependency relation
   */
  fun setDependencyRelation(dependent: Int, dependencyRelation: DependencyRelation) {
    this.dependencyRelations[dependent] = dependencyRelation
  }

  /**
   * Set a dependency relation to a given dependent with a [Deprel] only.
   *
   * @param dependent an element of the tree
   * @param deprel a deprel
   */
  fun setDependencyRelation(dependent: Int, deprel: Deprel) {
    this.dependencyRelations[dependent] = DependencyRelation(deprel = deprel)
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

    var head: Int? = this.getHead(element)
    val visited = mutableSetOf<Int>()

    while (head != null && head !in visited) {

      callback?.invoke(head)

      visited.add(head)

      head = this.getHead(head)
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

    var head: Int? = this.getHead(element)
    val visited = mutableSetOf<Int>()

    while (head != null && head !in visited) {

      if (callback(head)) return true

      visited.add(head)

      head = this.getHead(head)
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
  fun isRoot(element: Int) = this.getHead(element) == null

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

    this.elements.forEach { element ->

      if (element !in visited) {

        var head: Int? = this.getHead(element)
        val visitedHeads = mutableSetOf<Int>()

        while (head != null) {

          if (head in visitedHeads) return true

          visitedHeads.add(head)

          head = this.getHead(head)
        }

        visited.add(element)
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
  fun isNonProjectiveArc(dependent: Int): Boolean =

    this.getHead(dependent)?.let { head ->

      val dependentPosition: Int = this.getPosition(dependent)
      val headPosition: Int = this.getPosition(head)

      val middleElementsPositions =
        IntRange(minOf(headPosition, dependentPosition) + 1, maxOf(headPosition, dependentPosition) - 1)

      // optimized with negated condition
      middleElementsPositions.any { position ->
        !this.isAncestorOf(candidateAncestor = head, element = this.elements[position])
      }

    } ?: false

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

    /**
     * @param element an element of the tree
     *
     * @return the list of in-order elements visiting the tree in depth starting from the given [element]
     */
    fun getInOrderElements(element: Int): List<Int> {

      val inOrderElements = mutableListOf<Int>()
      val position: Int = this.getPosition(element)

      this.elements.subList(0, position)
        .filter { this.getHead(it) == element }
        .forEach { inOrderElements.addAll(getInOrderElements(it)) }

      inOrderElements.add(element)

      this.elements.subList(position + 1, this.size)
        .filter { this.getHead(it) == element }
        .forEach { inOrderElements.addAll(getInOrderElements(it)) }

      return inOrderElements
    }

    require(this.isDAG()) { "Required a single directed acyclic graph."}

    return getInOrderElements(this.roots.first()) // Note: if it is a DAG there is only one root
  }

  /**
   * @return a map of elements to their in-order position index
   */
  fun elementsToInOrderIndex(): Map<Int, Int> =
    this.inOrder().withIndex().associate { (inOrderIndex, element) -> element to inOrderIndex }

  /**
   * @param otherTree another dependency tree
   *
   * @return a Boolean indicating whether the dependency relations of this tree and the given one match
   */
  fun matchDependencyRelations(otherTree: DependencyTree): Boolean =
    this.dependencyRelations == otherTree.dependencyRelations

  /**
   * @param otherTree another dependency tree
   *
   * @return a Boolean indicating whether the given tree heads match the heads of this [DependencyTree]
   */
  fun matchHeads(otherTree: DependencyTree): Boolean = this.heads == otherTree.heads

  /**
   * Get a list of paths that represent the cycles of this tree.
   *
   * @return the list of cycles
   */
  fun getCycles(): List<Path> {

    val cycles = mutableListOf<Path>()
    val visited = mutableSetOf<Int>()

    this.elements.forEach { element ->

      if (element !in visited) {

        var head: Int? = this.getHead(element)

        if (head != null) {

          val visitedHeads = mutableSetOf(element)

          while (head != null && head !in visited && head !in visitedHeads) {

            visitedHeads.add(head)

            head = this.getHead(head)
          }

          if (head != null && head in visitedHeads)
            cycles.add(this.getCycle(startElement = head))

          visited.addAll(visitedHeads)

        } else {
          visited.add(element)
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

    val tree = DependencyTree(this.elements)

    tree.roots.clear()
    tree.roots.addAll(this.roots)

    this.heads.forEach { element, head -> tree.heads[element] = head }
    this.dependencyRelations.forEach { element, relation -> tree.dependencyRelations[element] = relation }
    this.attachmentScores.forEach { element, score -> tree.attachmentScores[element] = score }
    this.leftDependents.forEach { element, dependents -> tree.leftDependents.getValue(element).addAll(dependents) }
    this.rightDependents.forEach { element, dependents -> tree.rightDependents.getValue(element).addAll(dependents) }

    return tree
  }

  /**
   * @return a Boolean indicating whether the given [other] object is equal to this [DependencyTree]
   */
  override operator fun equals(other: Any?): Boolean
    = other is DependencyTree && this.matchHeads(other) && this.matchDependencyRelations(other)

  /**
   * @return the hash code of this [DependencyTree]
   */
  override fun hashCode(): Int = this.heads.hashCode() * 8191 + this.dependencyRelations.hashCode()

  /**
   * Set the arc defined by the id, head and deprel of a given CoNLL [token].
   *
   * @param token a CoNLL token
   */
  private fun setArc(token: Token) {

    val head: Int = token.head!!

    val dependencyRelation = DependencyRelation(deprel = token.deprel, posTag = token.pos)

    if (head > 0)
      this.setArc(dependent = token.id, governor = head, dependencyRelation = dependencyRelation)
    else
      this.setDependencyRelation(dependent = token.id, dependencyRelation = dependencyRelation)
  }

  /**
   * Add the given dependent to the [leftDependents] or [rightDependents] of the given governor.
   *
   * @param dependent an element of the tree
   * @param governor an element of the tree
   */
  private fun addDependent(dependent: Int, governor: Int) {
    if (this.getPosition(dependent) < this.getPosition(governor))
      this.leftDependents.getValue(governor).add(dependent)
    else
      this.rightDependents.getValue(governor).add(dependent)
  }

  /**
   * Set the given element as root.
   *
   * @param element an element of the tree
   */
  private fun setRoot(element: Int) {

    val elementPosition: Int = this.getPosition(element)

    val index: Int = this.roots.indexOfFirst { this.getPosition(it) > elementPosition }
    val insertAt: Int = if (index >= 0) index else this.roots.size

    this.roots.add(insertAt, element)
  }

  /**
   * Remove the given [dependent] from the [leftDependents] or [rightDependents] of the given [governor].
   *
   * @param dependent an element of the tree
   * @param governor an element of the tree
   */
  private fun removeDependent(dependent: Int, governor: Int) {
    if (this.getPosition(dependent) < this.getPosition(governor))
      this.leftDependents.getValue(governor).remove(dependent)
    else
      this.rightDependents.getValue(governor).remove(dependent)
  }

  /**
   * @param startElement the start element of a cycle
   *
   * @return the cycle path built starting from [startElement]
   */
  private fun getCycle(startElement: Int): Path {

    val cycle = mutableListOf<Arc>()
    var head: Int = this.getHead(startElement)!!

    cycle.add(Arc(dependent = startElement, governor = head))

    while (head != startElement) {

      val nextHead: Int = this.getHead(head)!!

      cycle.add(Arc(dependent = head, governor = nextHead))

      head = nextHead
    }

    return Path(cycle)
  }
}
