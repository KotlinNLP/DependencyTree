/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

import com.kotlinnlp.dependencytree.DependencyTree
import com.kotlinnlp.dependencytree.configuration.DependencyConfiguration
import com.kotlinnlp.linguisticdescription.syntax.SyntacticDependency
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.lang.IllegalArgumentException
import kotlin.test.*

/**
 * DependencyTreeSpec.
 */
class DependencyTreeSpec : Spek({

  describe("a DependencyTree.Unlabeled built with the size") {

    context("not initialized") {

      val dependencyTree = DependencyTree.Unlabeled(size = 5)

      context("roots") {

        it("should have the expected roots") {
          assertEquals(listOf(0, 1, 2, 3, 4), dependencyTree.roots)
        }
      }

      context("attachment scores") {

        it("should have the expected default attachment scores") {
          assertTrue { dependencyTree.attachmentScores.values.all { it == 0.0 } }
        }
      }

      context("containsCycle") {

        it("should return false") {
          assertFalse { dependencyTree.containsCycle() }
        }
      }

      context("getCycles") {

        it("should return no cycles") {
          assertTrue { dependencyTree.getCycles().isEmpty() }
        }
      }

      context("toString") {

        val expectedString = """
          0 _ _

          1 _ _

          2 _ _

          3 _ _

          4 _ _
          """.trimIndent()

        it("should return the expected string") {
          assertEquals(expectedString, dependencyTree.toString())
        }
      }
    }

    /**
     * id    |   0    1    2    3    4
     * head  |  -1    2    4    2    3
     */
    context("pre-initialized with a cycle") {

      val dependencyTree = DependencyTree.Unlabeled(
        size = 5,
        dependencies = listOf(
          DependencyConfiguration.Unlabeled.Arc(1, 2),
          DependencyConfiguration.Unlabeled.Arc(2, 4),
          DependencyConfiguration.Unlabeled.Arc(3, 2),
          DependencyConfiguration.Unlabeled.Arc(4, 3)
        ),
        allowCycles = true)

      context("containsCycle") {

        it("should return true") {
          assertTrue { dependencyTree.containsCycle() }
        }
      }

      context("getCycles") {

        it("should return the expected cycles") {

          assertEquals(dependencyTree.getCycles(), listOf(
            DependencyTree.Path(arcs = listOf(
              DependencyTree.Arc(dependent = 2, governor = 4),
              DependencyTree.Arc(dependent = 4, governor = 3),
              DependencyTree.Arc(dependent = 3, governor = 2)
            )))
          )
        }
      }

      context("anyAncestor") {

        it("should return true on its ancestor condition") {
          assertTrue { dependencyTree.anyAncestor(2) { it == 2 } }
        }
      }

      context("isRoot") {

        it("should return true on a root element") {
          assertTrue { dependencyTree.isRoot(0) }
        }

        it("should return false on a non-root element") {
          assertFalse { dependencyTree.isRoot(1) }
        }
      }
    }

    /**
     * id    |   0    1    2    3    4
     * head  |  -1    0    0    4    0
     */
    context("pre-initialized with attachment scores") {

      val dependencyTree = DependencyTree.Unlabeled(
        size = 5,
        dependencies = listOf(
          DependencyConfiguration.Unlabeled.Arc(1, 0, attachmentScore = 0.5),
          DependencyConfiguration.Unlabeled.Arc(2, 0, attachmentScore = 0.2),
          DependencyConfiguration.Unlabeled.Arc(3, 4, attachmentScore = 0.7),
          DependencyConfiguration.Unlabeled.Arc(4, 0, attachmentScore = 0.3)
        ))

      it("should contain the expected attachment score of the element 0") {
        assertEquals(0.0, dependencyTree.getAttachmentScore(0))
      }

      it("should contain the expected attachment score of the element 1") {
        assertEquals(0.5, dependencyTree.getAttachmentScore(1))
      }

      it("should contain the expected attachment score of the element 2") {
        assertEquals(0.2, dependencyTree.getAttachmentScore(2))
      }

      it("should contain the expected attachment score of the element 3") {
        assertEquals(0.7, dependencyTree.getAttachmentScore(3))
      }

      it("should contain the expected attachment score of the element 4") {
        assertEquals(0.3, dependencyTree.getAttachmentScore(4))
      }
    }

    /**
     * id    |   0    1    2    3    4
     * head  |  -1    0    0    4    0
     */
    context("pre-initialized with a single root") {

      val dependencyTree = DependencyTree.Unlabeled(
        size = 5,
        dependencies = listOf(
          DependencyConfiguration.Unlabeled.Arc(1, 0),
          DependencyConfiguration.Unlabeled.Arc(2, 0),
          DependencyConfiguration.Unlabeled.Arc(3, 4),
          DependencyConfiguration.Unlabeled.Arc(4, 0)
        ))

      it("should have the expected root") {
        assertEquals(listOf(0), dependencyTree.roots)
      }

      it("should have the expected default attachment scores") {
        assertTrue { dependencyTree.attachmentScores.values.all { it == 0.0 } }
      }

      context("hasSingleRoot") {

        it("should return true"){
          assertTrue { dependencyTree.hasSingleRoot() }
        }
      }

      context("isRoot") {

        it("should return true on a root element") {
          assertTrue { dependencyTree.isRoot(0) }
        }

        it("should return false on a non-root element") {
          assertFalse { dependencyTree.isRoot(1) }
        }
      }

      context("isDAG") {

        it("should return true") {

          assertTrue { dependencyTree.isDAG() }
        }
      }

      context("containsCycle") {

        it("should return false") {
          assertFalse { dependencyTree.containsCycle() }
        }
      }

      context("getCycles") {

        it("should return no cycles") {
          assertTrue { dependencyTree.getCycles().isEmpty() }
        }
      }

      context("introduceCycle") {

        it("should detect the introduction of a cycle") {
          assertTrue { dependencyTree.introduceCycle(dependent = 4, governor = 3) }
        }
      }

      context("toString") {

        val expectedString = """
          0 _ _
          +--r 1 _ _
          +--r 2 _ _
          +--r 4 _ _
               +--l 3 _ _
          """.trimIndent()

        it("should return the expected string") {
          assertEquals(expectedString, dependencyTree.toString())
        }
      }
    }

    /**
     * id    |   0    1    2    3    4   ->   0    1    2    3    4
     * head  |  -1    0    0    4    0       -1    0   -1    4    0
     */
    context("pre-initialized with a single root (removing arcs)") {

      val dependencyTree = DependencyTree.Unlabeled(
        size = 5,
        dependencies = listOf(
          DependencyConfiguration.Unlabeled.Arc(1, 0),
          DependencyConfiguration.Unlabeled.Arc(2, 0),
          DependencyConfiguration.Unlabeled.Arc(3, 4),
          DependencyConfiguration.Unlabeled.Arc(4, 0)
        ))

      context("removing the arc between 2 and 0") {

        dependencyTree.removeArc(dependent = 2, governor = 0)

        it("should contain no single root") {
          assertFalse { dependencyTree.hasSingleRoot() }
        }

        it("should contain 2 as new root") {
          assertTrue { dependencyTree.isRoot(2) }
        }

        it("should return null as head of 2") {
          assertNull(dependencyTree.heads[2])
        }

        it("should not contain 2 as dependent of 0") {
          assertFalse { 2 in dependencyTree.getDependents(0) }
        }
      }
    }

    /**
     * id    |   0    1    2    3    4
     * head  |  -1    0    1    4   -1
     */
    context("pre-initialized with two roots") {

      val dependencyTree = DependencyTree.Unlabeled(
        size = 5,
        dependencies = listOf(
          DependencyConfiguration.Unlabeled.Root(0),
          DependencyConfiguration.Unlabeled.Arc(dependent = 1, governor = 0),
          DependencyConfiguration.Unlabeled.Arc(dependent = 2, governor = 1),
          DependencyConfiguration.Unlabeled.Arc(dependent = 3, governor = 4),
          DependencyConfiguration.Unlabeled.Root(4)
        ))

      it("should have the expected root") {
        assertEquals(listOf(0, 4), dependencyTree.roots)
      }

      context("hasSingleRoot") {

        it("should return true"){
          assertFalse { dependencyTree.hasSingleRoot() }
        }
      }

      context("isRoot") {

        it("should return true on a root element") {
          assertTrue { dependencyTree.isRoot(4) }
        }

        it("should return false on a non-root element") {
          assertFalse { dependencyTree.isRoot(1) }
        }
      }

      context("isDAG") {

        it("should return false") {

          assertFalse { dependencyTree.isDAG() }
        }
      }

      context("containsCycle") {

        it("should return false") {
          assertFalse { dependencyTree.containsCycle() }
        }
      }

      context("getCycles") {

        it("should return no cycles") {
          assertTrue { dependencyTree.getCycles().isEmpty() }
        }
      }

      context("inDepthPreOrder") {

        it("should raise an exception") {
          assertFailsWith<IllegalArgumentException> { dependencyTree.inDepthPreOrder() }
        }
      }

      context("inDepthPostOrder") {

        it("should raise an exception") {
          assertFailsWith<IllegalArgumentException> { dependencyTree.inDepthPostOrder() }
        }
      }

      context("inBreadthPreOrder") {

        it("should raise an exception") {
          assertFailsWith<IllegalArgumentException> { dependencyTree.inBreadthPreOrder() }
        }
      }

      context("inBreadthPostOrder") {

        it("should raise an exception") {
          assertFailsWith<IllegalArgumentException> { dependencyTree.inBreadthPostOrder() }
        }
      }

      context("forEachAncestor") {

        val ancestors = mutableListOf<Int>()

        dependencyTree.forEachAncestor(2) { ancestors.add(it) }

        it("should iterate over the expected ancestors of an element") {
          assertEquals(ancestors.toList(), listOf(1, 0))
        }
      }

      context("anyAncestor") {

        it("should return true on its ancestor condition") {
          assertTrue { dependencyTree.anyAncestor(2) { it == 0 } }
        }

        it("should return false on not ancestor condition") {
          assertFalse { dependencyTree.anyAncestor(2) { it == 4 } }
        }
      }

      context("isAncestorOf") {

        it("should return true on its ancestor condition") {
          assertTrue { dependencyTree.isAncestorOf(candidateAncestor = 0, element = 2) }
        }

        it("should return false on not ancestor condition") {
          assertFalse { dependencyTree.isAncestorOf(candidateAncestor = 4, element = 2) }
        }
      }

      context("introduceCycle") {

        it("should detect the feasibility of an arc") {
          assertFalse { dependencyTree.introduceCycle(dependent = 4, governor = 2) }
        }

        it("should detect the introduction of a cycle ") {
          assertTrue { dependencyTree.introduceCycle(dependent = 0, governor = 2) }
        }
      }

      context("isProjective") {

        it("should return true") {

          assertTrue { dependencyTree.isProjective() }
        }
      }

      context("toString") {

        val expectedString = """
          0 _ _
          +--r 1 _ _
               +--r 2 _ _

          4 _ _
          +--l 3 _ _
          """.trimIndent()

        it("should return the expected string") {
          assertEquals(expectedString, dependencyTree.toString())
        }
      }
    }

    /**
     * token | You cannot put flavor into a bean that is not already there
     * id    |  0    1     2    3     4   5   6   7   8   9    10     11
     * head  |  2    2    -1    2     6   6   2   8   3   8    11     8
     */
    context("pre-initialized with non-projective arcs") {

      val dependencyTree = DependencyTree.Unlabeled(
        size = 12,
        dependencies = listOf(
          DependencyConfiguration.Unlabeled.Arc(0, 2),
          DependencyConfiguration.Unlabeled.Arc(1, 2),
          DependencyConfiguration.Unlabeled.Root(2),
          DependencyConfiguration.Unlabeled.Arc(3, 2),
          DependencyConfiguration.Unlabeled.Arc(4, 6),
          DependencyConfiguration.Unlabeled.Arc(5, 6),
          DependencyConfiguration.Unlabeled.Arc(6, 2),
          DependencyConfiguration.Unlabeled.Arc(7, 8),
          DependencyConfiguration.Unlabeled.Arc(8, 3),
          DependencyConfiguration.Unlabeled.Arc(9, 8),
          DependencyConfiguration.Unlabeled.Arc(10, 11),
          DependencyConfiguration.Unlabeled.Arc(11, 8)
        ))

      context("isRoot") {

        it("should return true on a root element") {
          assertTrue { dependencyTree.isRoot(2) }
        }

        it("should return false on a non-root element") {
          assertFalse { dependencyTree.isRoot(6) }
        }
      }

      context("isDAG") {

        it("should return true") {

          assertTrue { dependencyTree.isDAG() }
        }
      }

      context("inDepthPreOrder") {

        it("should return the expected list") {
          assertEquals(listOf(2, 0, 1, 3, 8, 7, 9, 11, 10, 6, 4, 5), dependencyTree.inDepthPreOrder())
        }
      }

      context("inDepthPostOrder") {

        it("should return the expected list") {
          assertEquals(listOf(0, 1, 7, 9, 10, 11, 8, 3, 4, 5, 6, 2), dependencyTree.inDepthPostOrder())
        }
      }

      context("inBreadthPreOrder") {

        it("should return the expected list") {
          assertEquals(listOf(2, 0, 1, 3, 6, 8, 4, 5, 7, 9, 11, 10), dependencyTree.inBreadthPreOrder())
        }
      }

      context("inBreadthPostOrder") {

        it("should return the expected list") {
          assertEquals(listOf(10, 7, 9, 11, 8, 4, 5, 0, 1, 3, 6, 2), dependencyTree.inBreadthPostOrder())
        }
      }

      context("inOrder") {

        it("should return the expected list") {
          assertEquals(listOf(0, 1, 2, 3, 7, 8, 9, 10, 11, 4, 5, 6), dependencyTree.inOrder())
        }
      }

      context("elementsToInOrderIndex") {

        it("should return the expected list") {
          assertEquals(
            mapOf(
              0 to 0,
              1 to 1,
              2 to 2,
              3 to 3,
              7 to 4,
              8 to 5,
              9 to 6,
              10 to 7,
              11 to 8,
              4 to 9,
              5 to 10,
              6 to 11),
            dependencyTree.elementsToInOrderIndex())
        }
      }

      context("containsCycle") {

        it("should return false") {
          assertFalse { dependencyTree.containsCycle() }
        }
      }

      context("getCycles") {

        it("should return no cycles") {
          assertTrue { dependencyTree.getCycles().isEmpty() }
        }
      }

      context("forEachAncestor") {

        val ancestors = mutableListOf<Int>()

        dependencyTree.forEachAncestor(10) { ancestors.add(it) }

        it("should iterate over the expected ancestors of an element") {
          assertEquals(ancestors.toList(), listOf(11, 8, 3, 2))
        }

        it("should return false on not ancestor condition") {
          assertFalse { dependencyTree.anyAncestor(10) { it == 5 } }
        }
      }

      context("anyAncestor") {

        it("should return true on its ancestor condition") {
          assertTrue { dependencyTree.anyAncestor(10) { it == 2 } }
        }

        it("should return false on not ancestor condition") {
          assertFalse { dependencyTree.anyAncestor(10) { it == 5 } }
        }
      }

      context("isAncestorOf") {

        it("should return true on its ancestor condition") {
          assertTrue { dependencyTree.isAncestorOf(candidateAncestor = 2, element = 10) }
        }

        it("should return false on not ancestor condition") {
          assertFalse { dependencyTree.isAncestorOf(candidateAncestor = 5, element = 10) }
        }
      }

      context("isNonProjectiveArc") {

        it("should return false at index 0") {
          assertFalse(dependencyTree.isNonProjectiveArc(0))
        }

        it("should return false at index 1") {
          assertFalse(dependencyTree.isNonProjectiveArc(1))
        }

        it("should return false at index 2") {
          assertFalse(dependencyTree.isNonProjectiveArc(2))
        }

        it("should return false at index 3") {
          assertFalse(dependencyTree.isNonProjectiveArc(3))
        }

        it("should return false at index 4") {
          assertFalse(dependencyTree.isNonProjectiveArc(4))
        }

        it("should return false at index 5") {
          assertFalse(dependencyTree.isNonProjectiveArc(5))
        }

        it("should return false at index 6") {
          assertFalse(dependencyTree.isNonProjectiveArc(6))
        }

        it("should return false at index 7") {
          assertFalse(dependencyTree.isNonProjectiveArc(7))
        }

        it("should return false at index 8") {
          assertTrue(dependencyTree.isNonProjectiveArc(8))
        }

        it("should return false at index 9") {
          assertFalse(dependencyTree.isNonProjectiveArc(9))
        }

        it("should return false at index 10") {
          assertFalse(dependencyTree.isNonProjectiveArc(10))
        }

        it("should return false at index 11") {
          assertFalse(dependencyTree.isNonProjectiveArc(11))
        }
      }

      context("isNonProjective") {

        it("should return true") {

          assertTrue { dependencyTree.isNonProjective() }
        }
      }

      context("getDescendants") {

        it("should raise an exception with a negative maxDepth") {
          assertFailsWith<IllegalArgumentException> { dependencyTree.getDescendants(3, maxDepth = -3) }
        }

        it("should raise an exception with maxDepth = 0") {
          assertFailsWith<IllegalArgumentException> { dependencyTree.getDescendants(3, maxDepth = 0) }
        }

        it("should return the expected descendants list with maxDepth = 1") {
          assertEquals(listOf(8), dependencyTree.getDescendants(3, maxDepth = 1))
        }

        it("should return the expected descendants list with maxDepth = 2") {
          assertEquals(listOf(8, 7, 9, 11), dependencyTree.getDescendants(3, maxDepth = 2))
        }
      }

      context("getAllDescendants") {

        it("should return the expected descendants list") {
          assertEquals(listOf(8, 7, 9, 11, 10), dependencyTree.getAllDescendants(3))
        }
      }

      context("toString") {

        val expectedString = """
          2 _ _
          +--l 0 _ _
          +--l 1 _ _
          +--r 3 _ _
          |    +--r 8 _ _
          |         +--l 7 _ _
          |         +--r 9 _ _
          |         +--r 11 _ _
          |              +--l 10 _ _
          +--r 6 _ _
               +--l 4 _ _
               +--l 5 _ _
               """.trimIndent()

        it("should return the expected string") {
          assertEquals(expectedString, dependencyTree.toString())
        }
      }

      context("toString(words)") {

        val words = listOf("You", "cannot", "put", "flavor", "into", "a", "bean", "that", "is", "not", "already",
          "there")

        val expectedString = """
          put _ _
          +--l You _ _
          +--l cannot _ _
          +--r flavor _ _
          |    +--r is _ _
          |         +--l that _ _
          |         +--r not _ _
          |         +--r there _ _
          |              +--l already _ _
          +--r bean _ _
               +--l into _ _
               +--l a _ _
          """.trimIndent()

        it("should return the expected string") {
          assertEquals(expectedString, dependencyTree.toString(words))
        }
      }
    }

    /**
     * id    |  0    1     2    3     4   5   6   7   8   9    10     11
     * head  |  2    2    -1    2     6   6   2   8   3   8    11     3
     */
    context("pre-initialized with overlapping 'continue' string paddings") {

      val dependencyTree = DependencyTree.Unlabeled(
        size = 12,
        dependencies = listOf(
          DependencyConfiguration.Unlabeled.Arc(0, 2),
          DependencyConfiguration.Unlabeled.Arc(1, 2),
          DependencyConfiguration.Unlabeled.Root(2),
          DependencyConfiguration.Unlabeled.Arc(3, 2),
          DependencyConfiguration.Unlabeled.Arc(4, 6),
          DependencyConfiguration.Unlabeled.Arc(5, 6),
          DependencyConfiguration.Unlabeled.Arc(6, 2),
          DependencyConfiguration.Unlabeled.Arc(7, 8),
          DependencyConfiguration.Unlabeled.Arc(8, 3),
          DependencyConfiguration.Unlabeled.Arc(9, 8),
          DependencyConfiguration.Unlabeled.Arc(10, 11),
          DependencyConfiguration.Unlabeled.Arc(11, 3)
        ))

      context("toString") {

        val expectedString = """
          2 _ _
          +--l 0 _ _
          +--l 1 _ _
          +--r 3 _ _
          |    +--r 8 _ _
          |    |    +--l 7 _ _
          |    |    +--r 9 _ _
          |    +--r 11 _ _
          |         +--l 10 _ _
          +--r 6 _ _
               +--l 4 _ _
               +--l 5 _ _
          """.trimIndent()

        it("should return the expected string") {
          assertEquals(expectedString, dependencyTree.toString())
        }
      }
    }
  }

  describe("a DependencyTree.Unlabeled built with a list of elements") {

    context("not initialized") {

      val dependencyTree = DependencyTree.Unlabeled(elements = listOf(5, 89, 13, 67, 69))

      context("roots") {

        it("should have the expected roots") {
          assertEquals(listOf(5, 89, 13, 67, 69), dependencyTree.roots)
        }
      }

      context("attachment scores") {

        it("should have the expected default attachment scores") {
          assertTrue { dependencyTree.attachmentScores.values.all { it == 0.0 } }
        }
      }

      context("containsCycle") {

        it("should return false") {
          assertFalse { dependencyTree.containsCycle() }
        }
      }

      context("getCycles") {

        it("should return no cycles") {
          assertTrue { dependencyTree.getCycles().isEmpty() }
        }
      }

      context("toString") {

        val expectedString = """
          5 _ _

          89 _ _

          13 _ _

          67 _ _

          69 _ _
          """.trimIndent()

        it("should return the expected string") {
          assertEquals(expectedString, dependencyTree.toString())
        }
      }
    }

    /**
     * id    |   5   89   13   67   69
     * head  |  -1   13   69   13   67
     */
    context("pre-initialized with a cycle") {

      val dependencyTree = DependencyTree.Unlabeled(
        elements = listOf(5, 89, 13, 67, 69),
        dependencies = listOf(
          DependencyConfiguration.Unlabeled.Arc(89, 13),
          DependencyConfiguration.Unlabeled.Arc(13, 69),
          DependencyConfiguration.Unlabeled.Arc(67, 13),
          DependencyConfiguration.Unlabeled.Arc(69, 67)
        ),
        allowCycles = true)

      context("containsCycle") {

        it("should return true") {
          assertTrue { dependencyTree.containsCycle() }
        }
      }

      context("getCycles") {

        it("should return the expected cycles") {

          assertEquals(dependencyTree.getCycles(), listOf(
            DependencyTree.Path(arcs = listOf(
              DependencyTree.Arc(dependent = 13, governor = 69),
              DependencyTree.Arc(dependent = 69, governor = 67),
              DependencyTree.Arc(dependent = 67, governor = 13)
            )))
          )
        }
      }

      context("anyAncestor") {

        it("should return true on its ancestor condition") {
          assertTrue { dependencyTree.anyAncestor(13) { it == 13 } }
        }
      }

      context("isRoot") {

        it("should return true on a root element") {
          assertTrue { dependencyTree.isRoot(5) }
        }

        it("should return false on a non-root element") {
          assertFalse { dependencyTree.isRoot(89) }
        }
      }
    }

    /**
     * id    |   5   89   13   67   69
     * head  |  -1    5    5   69    5
     */
    context("pre-initialized with attachment scores") {

      val dependencyTree = DependencyTree.Unlabeled(
        elements = listOf(5, 89, 13, 67, 69),
        dependencies = listOf(
          DependencyConfiguration.Unlabeled.Arc(89, 5, attachmentScore = 0.5),
          DependencyConfiguration.Unlabeled.Arc(13, 5, attachmentScore = 0.2),
          DependencyConfiguration.Unlabeled.Arc(67, 69, attachmentScore = 0.7),
          DependencyConfiguration.Unlabeled.Arc(69, 5, attachmentScore = 0.3)
        ))

      it("should contain the expected attachment score of the element 5") {
        assertEquals(0.0, dependencyTree.getAttachmentScore(5))
      }

      it("should contain the expected attachment score of the element 89") {
        assertEquals(0.5, dependencyTree.getAttachmentScore(89))
      }

      it("should contain the expected attachment score of the element 13") {
        assertEquals(0.2, dependencyTree.getAttachmentScore(13))
      }

      it("should contain the expected attachment score of the element 67") {
        assertEquals(0.7, dependencyTree.getAttachmentScore(67))
      }

      it("should contain the expected attachment score of the element 69") {
        assertEquals(0.3, dependencyTree.getAttachmentScore(69))
      }
    }

    /**
     * id    |   5   89   13   67   69
     * head  |  -1    5    5   69    5
     */
    context("pre-initialized with a single root") {

      val dependencyTree = DependencyTree.Unlabeled(
        elements = listOf(5, 89, 13, 67, 69),
        dependencies = listOf(
          DependencyConfiguration.Unlabeled.Arc(89, 5),
          DependencyConfiguration.Unlabeled.Arc(13, 5),
          DependencyConfiguration.Unlabeled.Arc(67, 69),
          DependencyConfiguration.Unlabeled.Arc(69, 5)
        ))

      it("should have the expected root") {
        assertEquals(listOf(5), dependencyTree.roots)
      }

      it("should have the expected default attachment scores") {
        assertTrue { dependencyTree.attachmentScores.values.all { it == 0.0 } }
      }

      context("hasSingleRoot") {

        it("should return true"){
          assertTrue { dependencyTree.hasSingleRoot() }
        }
      }

      context("isRoot") {

        it("should return true on a root element") {
          assertTrue { dependencyTree.isRoot(5) }
        }

        it("should return false on a non-root element") {
          assertFalse { dependencyTree.isRoot(89) }
        }
      }

      context("isDAG") {

        it("should return true") {

          assertTrue { dependencyTree.isDAG() }
        }
      }

      context("containsCycle") {

        it("should return false") {
          assertFalse { dependencyTree.containsCycle() }
        }
      }

      context("getCycles") {

        it("should return no cycles") {
          assertTrue { dependencyTree.getCycles().isEmpty() }
        }
      }

      context("introduceCycle") {

        it("should detect the introduction of a cycle") {
          assertTrue { dependencyTree.introduceCycle(dependent = 69, governor = 67) }
        }
      }

      context("toString") {

        val expectedString = """
          5 _ _
          +--r 89 _ _
          +--r 13 _ _
          +--r 69 _ _
               +--l 67 _ _
          """.trimIndent()

        it("should return the expected string") {
          assertEquals(expectedString, dependencyTree.toString())
        }
      }
    }

    /**
     * id    |   5   89   13   67   69   ->   5   89   13   67   69
     * head  |  -1    5    5   69    5       -1    5   -1   69    5
     */
    context("pre-initialized with a single root (removing arcs)") {

      val dependencyTree = DependencyTree.Unlabeled(
        elements = listOf(5, 89, 13, 67, 69),
        dependencies = listOf(
          DependencyConfiguration.Unlabeled.Arc(89, 5),
          DependencyConfiguration.Unlabeled.Arc(13, 5),
          DependencyConfiguration.Unlabeled.Arc(67, 69),
          DependencyConfiguration.Unlabeled.Arc(69, 5)
        ))

      context("removing the arc between 13 and 5") {

        dependencyTree.removeArc(dependent = 13, governor = 5)

        it("should contain no single root") {
          assertFalse { dependencyTree.hasSingleRoot() }
        }

        it("should contain 2 as new root") {
          assertTrue { dependencyTree.isRoot(13) }
        }

        it("should return null as head of 13") {
          assertNull(dependencyTree.heads[13])
        }

        it("should not contain 13 as dependent of 5") {
          assertFalse { 13 in dependencyTree.getDependents(5) }
        }
      }
    }

    /**
     * id    |   5   89   13   67   69
     * head  |  -1    5   89   69   -1
     */
    context("pre-initialized with two roots") {

      val dependencyTree = DependencyTree.Unlabeled(
        elements = listOf(5, 89, 13, 67, 69),
        dependencies = listOf(
          DependencyConfiguration.Unlabeled.Root(5),
          DependencyConfiguration.Unlabeled.Arc(dependent = 89, governor = 5),
          DependencyConfiguration.Unlabeled.Arc(dependent = 13, governor =89),
          DependencyConfiguration.Unlabeled.Arc(dependent = 67, governor = 69),
          DependencyConfiguration.Unlabeled.Root(69)
        ))

      it("should have the expected root") {
        assertEquals(listOf(5, 69), dependencyTree.roots)
      }

      context("hasSingleRoot") {

        it("should return true"){
          assertFalse { dependencyTree.hasSingleRoot() }
        }
      }

      context("isRoot") {

        it("should return true on a root element") {
          assertTrue { dependencyTree.isRoot(69) }
        }

        it("should return false on a non-root element") {
          assertFalse { dependencyTree.isRoot(89) }
        }
      }

      context("isDAG") {

        it("should return false") {

          assertFalse { dependencyTree.isDAG() }
        }
      }

      context("containsCycle") {

        it("should return false") {
          assertFalse { dependencyTree.containsCycle() }
        }
      }

      context("getCycles") {

        it("should return no cycles") {
          assertTrue { dependencyTree.getCycles().isEmpty() }
        }
      }

      context("inDepthPreOrder") {

        it("should raise an exception") {
          assertFailsWith<IllegalArgumentException> { dependencyTree.inDepthPreOrder() }
        }
      }

      context("inDepthPostOrder") {

        it("should raise an exception") {
          assertFailsWith<IllegalArgumentException> { dependencyTree.inDepthPostOrder() }
        }
      }

      context("inBreadthPreOrder") {

        it("should raise an exception") {
          assertFailsWith<IllegalArgumentException> { dependencyTree.inBreadthPreOrder() }
        }
      }

      context("inBreadthPostOrder") {

        it("should raise an exception") {
          assertFailsWith<IllegalArgumentException> { dependencyTree.inBreadthPostOrder() }
        }
      }

      context("forEachAncestor") {

        val ancestors = mutableListOf<Int>()

        dependencyTree.forEachAncestor(13) { ancestors.add(it) }

        it("should iterate over the expected ancestors of an element") {
          assertEquals(ancestors.toList(), listOf(89, 5))
        }
      }

      context("anyAncestor") {

        it("should return true on its ancestor condition") {
          assertTrue { dependencyTree.anyAncestor(13) { it == 5 } }
        }

        it("should return false on not ancestor condition") {
          assertFalse { dependencyTree.anyAncestor(13) { it == 69 } }
        }
      }

      context("isAncestorOf") {

        it("should return true on its ancestor condition") {
          assertTrue { dependencyTree.isAncestorOf(candidateAncestor = 5, element = 13) }
        }

        it("should return false on not ancestor condition") {
          assertFalse { dependencyTree.isAncestorOf(candidateAncestor = 69, element = 13) }
        }
      }

      context("introduceCycle") {

        it("should detect the feasibility of an arc") {
          assertFalse { dependencyTree.introduceCycle(dependent = 69, governor = 13) }
        }

        it("should detect the introduction of a cycle ") {
          assertTrue { dependencyTree.introduceCycle(dependent = 5, governor = 13) }
        }
      }

      context("isProjective") {

        it("should return true") {

          assertTrue { dependencyTree.isProjective() }
        }
      }

      context("toString") {

        val expectedString = """
          5 _ _
          +--r 89 _ _
               +--r 13 _ _

          69 _ _
          +--l 67 _ _
          """.trimIndent()

        it("should return the expected string") {
          assertEquals(expectedString, dependencyTree.toString())
        }
      }
    }

    /**
     * token | You cannot put flavor into a bean that is not already there
     * id    |  5    89   13    67    69  97  15   3  45  77   14     52
     * head  |  13   13   -1    13    15  15  13  45  67  45   52     45
     */
    context("pre-initialized with non-projective arcs") {

      val dependencyTree = DependencyTree.Unlabeled(
        elements = listOf(5, 89, 13, 67, 69, 97, 15, 3, 45, 77, 14, 52),
        dependencies = listOf(
          DependencyConfiguration.Unlabeled.Arc(5, 13),
          DependencyConfiguration.Unlabeled.Arc(89, 13),
          DependencyConfiguration.Unlabeled.Root(13),
          DependencyConfiguration.Unlabeled.Arc(67, 13),
          DependencyConfiguration.Unlabeled.Arc(69, 15),
          DependencyConfiguration.Unlabeled.Arc(97, 15),
          DependencyConfiguration.Unlabeled.Arc(15, 13),
          DependencyConfiguration.Unlabeled.Arc(3, 45),
          DependencyConfiguration.Unlabeled.Arc(45, 67),
          DependencyConfiguration.Unlabeled.Arc(77, 45),
          DependencyConfiguration.Unlabeled.Arc(14, 52),
          DependencyConfiguration.Unlabeled.Arc(52, 45)
        ))

      context("isRoot") {

        it("should return true on a root element") {
          assertTrue { dependencyTree.isRoot(13) }
        }

        it("should return false on a non-root element") {
          assertFalse { dependencyTree.isRoot(15) }
        }
      }

      context("isDAG") {

        it("should return true") {

          assertTrue { dependencyTree.isDAG() }
        }
      }

      context("inDepthPreOrder") {

        it("should return the expected list") {
          assertEquals(listOf(13, 5, 89, 67, 45, 3, 77, 52, 14, 15, 69, 97), dependencyTree.inDepthPreOrder())
        }
      }

      context("inDepthPostOrder") {

        it("should return the expected list") {
          assertEquals(listOf(5, 89, 3, 77, 14, 52, 45, 67, 69, 97, 15, 13), dependencyTree.inDepthPostOrder())
        }
      }

      context("inBreadthPreOrder") {

        it("should return the expected list") {
          assertEquals(listOf(13, 5, 89, 67, 15, 45, 69, 97, 3, 77, 52, 14), dependencyTree.inBreadthPreOrder())
        }
      }

      context("inBreadthPostOrder") {

        it("should return the expected list") {
          assertEquals(listOf(14, 3, 77, 52, 45, 69, 97, 5, 89, 67, 15, 13), dependencyTree.inBreadthPostOrder())
        }
      }

      context("inOrder") {

        it("should return the expected list") {
          assertEquals(listOf(5, 89, 13, 67, 3, 45, 77, 14, 52, 69, 97, 15), dependencyTree.inOrder())
        }
      }

      context("elementsToInOrderIndex") {

        it("should return the expected list") {
          assertEquals(
            mapOf(
              5 to 0,
              89 to 1,
              13 to 2,
              67 to 3,
              3 to 4,
              45 to 5,
              77 to 6,
              14 to 7,
              52 to 8,
              69 to 9,
              97 to 10,
              15 to 11),
            dependencyTree.elementsToInOrderIndex())
        }
      }

      context("containsCycle") {

        it("should return false") {
          assertFalse { dependencyTree.containsCycle() }
        }
      }

      context("getCycles") {

        it("should return no cycles") {
          assertTrue { dependencyTree.getCycles().isEmpty() }
        }
      }

      context("forEachAncestor") {

        val ancestors = mutableListOf<Int>()

        dependencyTree.forEachAncestor(14) { ancestors.add(it) }

        it("should iterate over the expected ancestors of an element") {
          assertEquals(ancestors.toList(), listOf(52, 45, 67, 13))
        }

        it("should return false on not ancestor condition") {
          assertFalse { dependencyTree.anyAncestor(14) { it == 97 } }
        }
      }

      context("anyAncestor") {

        it("should return true on its ancestor condition") {
          assertTrue { dependencyTree.anyAncestor(14) { it == 13 } }
        }

        it("should return false on not ancestor condition") {
          assertFalse { dependencyTree.anyAncestor(14) { it == 97 } }
        }
      }

      context("isAncestorOf") {

        it("should return true on its ancestor condition") {
          assertTrue { dependencyTree.isAncestorOf(candidateAncestor = 13, element = 14) }
        }

        it("should return false on not ancestor condition") {
          assertFalse { dependencyTree.isAncestorOf(candidateAncestor = 97, element = 14) }
        }
      }

      context("isNonProjectiveArc") {

        it("should return false for the token 5") {
          assertFalse(dependencyTree.isNonProjectiveArc(5))
        }

        it("should return false for the token 89") {
          assertFalse(dependencyTree.isNonProjectiveArc(89))
        }

        it("should return false for the token 13") {
          assertFalse(dependencyTree.isNonProjectiveArc(13))
        }

        it("should return false for the token 67") {
          assertFalse(dependencyTree.isNonProjectiveArc(67))
        }

        it("should return false for the token 69") {
          assertFalse(dependencyTree.isNonProjectiveArc(69))
        }

        it("should return false for the token 97") {
          assertFalse(dependencyTree.isNonProjectiveArc(97))
        }

        it("should return false for the token 15") {
          assertFalse(dependencyTree.isNonProjectiveArc(15))
        }

        it("should return false for the token 3") {
          assertFalse(dependencyTree.isNonProjectiveArc(3))
        }

        it("should return false for the token 45") {
          assertTrue(dependencyTree.isNonProjectiveArc(45))
        }

        it("should return false for the token 77") {
          assertFalse(dependencyTree.isNonProjectiveArc(77))
        }

        it("should return false for the token 14") {
          assertFalse(dependencyTree.isNonProjectiveArc(14))
        }

        it("should return false for the token 52") {
          assertFalse(dependencyTree.isNonProjectiveArc(52))
        }
      }

      context("isNonProjective") {

        it("should return true") {

          assertTrue { dependencyTree.isNonProjective() }
        }
      }

      context("getDescendants") {

        it("should raise an exception with a negative maxDepth") {
          assertFailsWith<IllegalArgumentException> { dependencyTree.getDescendants(3, maxDepth = -3) }
        }

        it("should raise an exception with maxDepth = 0") {
          assertFailsWith<IllegalArgumentException> { dependencyTree.getDescendants(3, maxDepth = 0) }
        }

        it("should return the expected descendants list with maxDepth = 1") {
          assertEquals(listOf(45), dependencyTree.getDescendants(67, maxDepth = 1))
        }

        it("should return the expected descendants list with maxDepth = 2") {
          assertEquals(listOf(45, 3, 77, 52), dependencyTree.getDescendants(67, maxDepth = 2))
        }
      }

      context("getAllDescendants") {

        it("should return the expected descendants list") {
          assertEquals(listOf(45, 3, 77, 52, 14), dependencyTree.getAllDescendants(67))
        }
      }

      context("toString") {

        val expectedString = """
          13 _ _
          +--l 5 _ _
          +--l 89 _ _
          +--r 67 _ _
          |    +--r 45 _ _
          |         +--l 3 _ _
          |         +--r 77 _ _
          |         +--r 52 _ _
          |              +--l 14 _ _
          +--r 15 _ _
               +--l 69 _ _
               +--l 97 _ _
               """.trimIndent()

        it("should return the expected string") {
          assertEquals(expectedString, dependencyTree.toString())
        }
      }

      context("toString(words)") {

        val words = listOf("You", "cannot", "put", "flavor", "into", "a", "bean", "that", "is", "not", "already",
          "there")

        val expectedString = """
          put _ _
          +--l You _ _
          +--l cannot _ _
          +--r flavor _ _
          |    +--r is _ _
          |         +--l that _ _
          |         +--r not _ _
          |         +--r there _ _
          |              +--l already _ _
          +--r bean _ _
               +--l into _ _
               +--l a _ _
          """.trimIndent()

        it("should return the expected string") {
          assertEquals(expectedString, dependencyTree.toString(words))
        }
      }
    }

    /**
     * token | You cannot put flavor into a bean that is not already there
     * id    |  5    89   13    67    69  97  15   3  45  77   14     52
     * head  |  13   13   -1    13    15  15  13  45  67  45   52     67
     */
    context("pre-initialized with overlapping 'continue' string paddings") {

      val dependencyTree = DependencyTree.Unlabeled(
        elements = listOf(5, 89, 13, 67, 69, 97, 15, 3, 45, 77, 14, 52),
        dependencies = listOf(
          DependencyConfiguration.Unlabeled.Arc(5, 13),
          DependencyConfiguration.Unlabeled.Arc(89, 13),
          DependencyConfiguration.Unlabeled.Root(13),
          DependencyConfiguration.Unlabeled.Arc(67, 13),
          DependencyConfiguration.Unlabeled.Arc(69, 15),
          DependencyConfiguration.Unlabeled.Arc(97, 15),
          DependencyConfiguration.Unlabeled.Arc(15, 13),
          DependencyConfiguration.Unlabeled.Arc(3, 45),
          DependencyConfiguration.Unlabeled.Arc(45, 67),
          DependencyConfiguration.Unlabeled.Arc(77, 45),
          DependencyConfiguration.Unlabeled.Arc(14, 52),
          DependencyConfiguration.Unlabeled.Arc(52, 67)
        ))

      context("toString") {

        val expectedString = """
          13 _ _
          +--l 5 _ _
          +--l 89 _ _
          +--r 67 _ _
          |    +--r 45 _ _
          |    |    +--l 3 _ _
          |    |    +--r 77 _ _
          |    +--r 52 _ _
          |         +--l 14 _ _
          +--r 15 _ _
               +--l 69 _ _
               +--l 97 _ _
          """.trimIndent()

        it("should return the expected string") {
          assertEquals(expectedString, dependencyTree.toString())
        }
      }
    }
  }

  describe("a DependencyTree.Labeled built with the size") {

    /**
     * id    |   0    1    2    3    4
     * head  |  -1    0    1    4   -1
     */
    context("pre-initialized with two roots") {

      val dependencyTree = DependencyTree.Labeled(
        size = 5,
        dependencies = listOf(
          DependencyConfiguration.Labeled.Root(
            id = 0,
            dependency = SyntacticDependency(annotation = "VERB", direction = SyntacticDependency.Direction.ROOT)),
          DependencyConfiguration.Labeled.Arc(
            dependent = 1,
            governor = 0,
            dependency = SyntacticDependency(annotation = "SUBJ", direction = SyntacticDependency.Direction.RIGHT)),
          DependencyConfiguration.Labeled.Arc(
            dependent = 2,
            governor = 1,
            dependency = SyntacticDependency(annotation = "RMOD", direction = SyntacticDependency.Direction.RIGHT)),
          DependencyConfiguration.Labeled.Arc(
            dependent = 3,
            governor = 4,
            dependency = SyntacticDependency(annotation = "PRON", direction = SyntacticDependency.Direction.LEFT)),
          DependencyConfiguration.Labeled.Root(
            id = 4,
            dependency = SyntacticDependency(annotation = "VERB", direction = SyntacticDependency.Direction.ROOT))
        ))

      context("matchesGrammar") {

        it("should match the expected grammatical configurations") {

          val testTree = DependencyTree.Labeled(listOf(0, 1, 2, 3, 4))

          testTree.setGrammaticalConfiguration(
            dependent = 0,
            dependency = SyntacticDependency(annotation = "VERB", direction = SyntacticDependency.Direction.ROOT))
          testTree.setGrammaticalConfiguration(
            dependent = 1,
            dependency = SyntacticDependency(annotation = "SUBJ", direction = SyntacticDependency.Direction.RIGHT))
          testTree.setGrammaticalConfiguration(
            dependent = 2,
            dependency = SyntacticDependency(annotation = "RMOD", direction = SyntacticDependency.Direction.RIGHT))
          testTree.setGrammaticalConfiguration(
            dependent = 3,
            dependency = SyntacticDependency(annotation = "PRON", direction = SyntacticDependency.Direction.LEFT))
          testTree.setGrammaticalConfiguration(
            dependent = 4,
            dependency = SyntacticDependency(annotation = "VERB", direction = SyntacticDependency.Direction.ROOT))

          assertTrue { dependencyTree.matchesGrammar(testTree) }
        }
      }

      context("toString") {

        val expectedString = """
          0 _ VERB
          +--r 1 _ SUBJ
               +--r 2 _ RMOD

          4 _ VERB
          +--l 3 _ PRON
          """.trimIndent()

        it("should return the expected string") {
          assertEquals(expectedString, dependencyTree.toString())
        }
      }
    }
  }

  describe("a DependencyTree.Labeled built with a list of elements") {

    /**
     * id    |   5   89   13   67   69
     * head  |  -1    5   89   69   -1
     */
    context("pre-initialized with two roots") {

      val dependencyTree = DependencyTree.Labeled(
        elements = listOf(5, 89, 13, 67, 69),
        dependencies = listOf(
          DependencyConfiguration.Labeled.Root(
            id = 5,
            dependency = SyntacticDependency(annotation = "VERB", direction = SyntacticDependency.Direction.ROOT)),
          DependencyConfiguration.Labeled.Arc(
            dependent = 89,
            governor = 5,
            dependency = SyntacticDependency(annotation = "SUBJ", direction = SyntacticDependency.Direction.RIGHT)),
          DependencyConfiguration.Labeled.Arc(
            dependent = 13,
            governor = 89,
            dependency = SyntacticDependency(annotation = "RMOD", direction = SyntacticDependency.Direction.RIGHT)),
          DependencyConfiguration.Labeled.Arc(
            dependent = 67,
            governor = 69,
            dependency = SyntacticDependency(annotation = "PRON", direction = SyntacticDependency.Direction.LEFT)),
          DependencyConfiguration.Labeled.Root(
            id = 69,
            dependency = SyntacticDependency(annotation = "VERB", direction = SyntacticDependency.Direction.ROOT))
        ))

      context("matchesGrammar") {

        it("should match the expected grammatical configurations") {

          val testTree = DependencyTree.Labeled(listOf(5, 89, 13, 67, 69))

          testTree.setGrammaticalConfiguration(
            dependent = 5,
            dependency = SyntacticDependency(annotation = "VERB", direction = SyntacticDependency.Direction.ROOT))
          testTree.setGrammaticalConfiguration(
            dependent = 89,
            dependency = SyntacticDependency(annotation = "SUBJ", direction = SyntacticDependency.Direction.RIGHT))
          testTree.setGrammaticalConfiguration(
            dependent = 13,
            dependency = SyntacticDependency(annotation = "RMOD", direction = SyntacticDependency.Direction.RIGHT))
          testTree.setGrammaticalConfiguration(
            dependent = 67,
            dependency = SyntacticDependency(annotation = "PRON", direction = SyntacticDependency.Direction.LEFT))
          testTree.setGrammaticalConfiguration(
            dependent = 69,
            dependency = SyntacticDependency(annotation = "VERB", direction = SyntacticDependency.Direction.ROOT))

          assertTrue { dependencyTree.matchesGrammar(testTree) }
        }
      }

      context("toString") {

        val expectedString = """
          5 _ VERB
          +--r 89 _ SUBJ
               +--r 13 _ RMOD

          69 _ VERB
          +--l 67 _ PRON
          """.trimIndent()

        it("should return the expected string") {
          assertEquals(expectedString, dependencyTree.toString())
        }
      }
    }
  }
})
