/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

import com.kotlinnlp.dependencytree.configuration.ArcConfiguration
import com.kotlinnlp.dependencytree.DependencyTree
import com.kotlinnlp.dependencytree.Deprel
import com.kotlinnlp.dependencytree.configuration.RootConfiguration
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * DependencyTreeSpec.
 */
class DependencyTreeSpec : Spek({

  describe("a DependencyTree") {

    context("not initialized") {

      val dependencyTree = DependencyTree(size = 5)

      on("roots") {

        it("should have the expected roots") {
          assertEquals(listOf(0, 1, 2, 3, 4), dependencyTree.roots)
        }
      }

      on("attachment scores") {

        it("should have the expected default attachment scores") {
          assertTrue { dependencyTree.attachmentScores.values.all { it == 0.0 } }
        }
      }

      on("containsCycle") {

        it("should return false") {
          assertFalse { dependencyTree.containsCycle() }
        }
      }

      on("getCycles") {

        it("should return no cycles") {
          assertTrue { dependencyTree.getCycles().isEmpty() }
        }
      }

      on("toString") {

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

      val dependencyTree = DependencyTree(
        size = 5,
        dependencies = listOf(
          ArcConfiguration(1, 2),
          ArcConfiguration(2, 4),
          ArcConfiguration(3, 2),
          ArcConfiguration(4, 3)
        ),
        allowCycles = true)

      on("containsCycle") {

        it("should return true") {
          assertTrue { dependencyTree.containsCycle() }
        }
      }

      on("getCycles") {

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

      on("anyAncestor") {

        it("should return true on its ancestor condition") {
          assertTrue { dependencyTree.anyAncestor(2) { it == 2 } }
        }
      }

      on("isRoot") {

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

      val dependencyTree = DependencyTree(
        size = 5,
        dependencies = listOf(
          ArcConfiguration(1, 0, attachmentScore = 0.5),
          ArcConfiguration(2, 0, attachmentScore = 0.2),
          ArcConfiguration(3, 4, attachmentScore = 0.7),
          ArcConfiguration(4, 0, attachmentScore = 0.3)
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

      val dependencyTree = DependencyTree(
        size = 5,
        dependencies = listOf(
          ArcConfiguration(1, 0),
          ArcConfiguration(2, 0),
          ArcConfiguration(3, 4),
          ArcConfiguration(4, 0)
        ))

      it("should have the expected root") {
        assertEquals(listOf(0), dependencyTree.roots)
      }

      it("should have the expected default attachment scores") {
        assertTrue { dependencyTree.attachmentScores.values.all { it == 0.0 } }
      }

      on("hasSingleRoot") {

        it("should return true"){
          assertTrue { dependencyTree.hasSingleRoot() }
        }
      }

      on("isRoot") {

        it("should return true on a root element") {
          assertTrue { dependencyTree.isRoot(0) }
        }

        it("should return false on a non-root element") {
          assertFalse { dependencyTree.isRoot(1) }
        }
      }

      on ("isDAG") {

        it("should return true") {

          assertTrue { dependencyTree.isDAG() }
        }
      }

      on("containsCycle") {

        it("should return false") {
          assertFalse { dependencyTree.containsCycle() }
        }
      }

      on("getCycles") {

        it("should return no cycles") {
          assertTrue { dependencyTree.getCycles().isEmpty() }
        }
      }

      on("introduceCycle") {

        it("should detect the introduction of a cycle") {
          assertTrue { dependencyTree.introduceCycle(dependent = 4, governor = 3) }
        }
      }

      on("toString") {

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

      val dependencyTree = DependencyTree(
        size = 5,
        dependencies = listOf(
          ArcConfiguration(1, 0),
          ArcConfiguration(2, 0),
          ArcConfiguration(3, 4),
          ArcConfiguration(4, 0)
        ))

      on("removing the arc between 2 and 0") {

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

        it("should return null as deprel of 2") {
          assertNull(dependencyTree.deprels[2])
        }

        it("should return null as POS tag of 2") {
          assertNull(dependencyTree.posTags[2])
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

      val dependencyTree = DependencyTree(
        size = 5,
        dependencies = listOf(
          RootConfiguration(id = 0, deprel = Deprel(label = "VERB", direction = Deprel.Position.ROOT)),
          ArcConfiguration(1, 0, deprel = Deprel(label = "SUBJ", direction = Deprel.Position.RIGHT)),
          ArcConfiguration(2, 1, deprel = null),
          ArcConfiguration(3, 4, deprel = Deprel(label = "PRON", direction = Deprel.Position.LEFT)),
          RootConfiguration(id = 4, deprel = Deprel(label = "VERB", direction = Deprel.Position.ROOT))
        ))

      it("should have the expected root") {
        assertEquals(listOf(0, 4), dependencyTree.roots)
      }

      on("hasSingleRoot") {

        it("should return true"){
          assertFalse { dependencyTree.hasSingleRoot() }
        }
      }

      on("isRoot") {

        it("should return true on a root element") {
          assertTrue { dependencyTree.isRoot(4) }
        }

        it("should return false on a non-root element") {
          assertFalse { dependencyTree.isRoot(1) }
        }
      }

      on ("isDAG") {

        it("should return false") {

          assertFalse { dependencyTree.isDAG() }
        }
      }

      on("containsCycle") {

        it("should return false") {
          assertFalse { dependencyTree.containsCycle() }
        }
      }

      on("getCycles") {

        it("should return no cycles") {
          assertTrue { dependencyTree.getCycles().isEmpty() }
        }
      }

      on("forEachAncestor") {

        val ancestors = mutableListOf<Int>()

        dependencyTree.forEachAncestor(2) { ancestors.add(it) }

        it("should iterate over the expected ancestors of an element") {
          assertEquals(ancestors.toList(), listOf(1, 0))
        }
      }

      on("anyAncestor") {

        it("should return true on its ancestor condition") {
          assertTrue { dependencyTree.anyAncestor(2) { it == 0 } }
        }

        it("should return false on not ancestor condition") {
          assertFalse { dependencyTree.anyAncestor(2) { it == 4 } }
        }
      }

      on("isAncestorOf") {

        it("should return true on its ancestor condition") {
          assertTrue { dependencyTree.isAncestorOf(candidateAncestor = 0, element = 2) }
        }

        it("should return false on not ancestor condition") {
          assertFalse { dependencyTree.isAncestorOf(candidateAncestor = 4, element = 2) }
        }
      }

      on("introduceCycle") {

        it("should detect the feasibility of an arc") {
          assertFalse { dependencyTree.introduceCycle(dependent = 4, governor = 2) }
        }

        it("should detect the introduction of a cycle ") {
          assertTrue { dependencyTree.introduceCycle(dependent = 0, governor = 2) }
        }
      }

      on("isProjective") {

        it("should return true") {

          assertTrue { dependencyTree.isProjective() }
        }
      }

      on("matchDeprels") {

        it("should match the expected deprels") {

          val testTree = DependencyTree(listOf(0, 1, 2, 3, 4))

          testTree.setDeprel(dependent = 0, deprel = Deprel(label = "VERB", direction = Deprel.Position.ROOT))
          testTree.setDeprel(dependent = 1, deprel = Deprel(label = "SUBJ", direction = Deprel.Position.RIGHT))
          testTree.setDeprel(dependent = 3, deprel = Deprel(label = "PRON", direction = Deprel.Position.LEFT))
          testTree.setDeprel(dependent = 4, deprel = Deprel(label = "VERB", direction = Deprel.Position.ROOT))

          assertTrue { dependencyTree.matchDeprels(testTree) }
        }
      }

      on("toString") {

        val expectedString = """
          0 _ VERB:ROOT
          +--r 1 _ SUBJ:RIGHT
               +--r 2 _ _

          4 _ VERB:ROOT
          +--l 3 _ PRON:LEFT
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

      val dependencyTree = DependencyTree(
        size = 12,
        dependencies = listOf(
          ArcConfiguration(0, 2),
          ArcConfiguration(1, 2),
          RootConfiguration(2),
          ArcConfiguration(3, 2),
          ArcConfiguration(4, 6),
          ArcConfiguration(5, 6),
          ArcConfiguration(6, 2),
          ArcConfiguration(7, 8),
          ArcConfiguration(8, 3),
          ArcConfiguration(9, 8),
          ArcConfiguration(10, 11),
          ArcConfiguration(11, 8)
        ))

      on("isRoot") {

        it("should return true on a root element") {
          assertTrue { dependencyTree.isRoot(2) }
        }

        it("should return false on a non-root element") {
          assertFalse { dependencyTree.isRoot(6) }
        }
      }

      on ("isDAG") {

        it("should return true") {

          assertTrue { dependencyTree.isDAG() }
        }
      }

      on("inOrder") {

        it("should return the expected list") {
          assertEquals(listOf(0, 1, 2, 3, 7, 8, 9, 10, 11, 4, 5, 6), dependencyTree.inOrder())
        }
      }

      on("elementsToInOrderIndex") {

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

      on("containsCycle") {

        it("should return false") {
          assertFalse { dependencyTree.containsCycle() }
        }
      }

      on("getCycles") {

        it("should return no cycles") {
          assertTrue { dependencyTree.getCycles().isEmpty() }
        }
      }

      on("forEachAncestor") {

        val ancestors = mutableListOf<Int>()

        dependencyTree.forEachAncestor(10) { ancestors.add(it) }

        it("should iterate over the expected ancestors of an element") {
          assertEquals(ancestors.toList(), listOf(11, 8, 3, 2))
        }

        it("should return false on not ancestor condition") {
          assertFalse { dependencyTree.anyAncestor(10) { it == 5 } }
        }
      }

      on("anyAncestor") {

        it("should return true on its ancestor condition") {
          assertTrue { dependencyTree.anyAncestor(10) { it == 2 } }
        }

        it("should return false on not ancestor condition") {
          assertFalse { dependencyTree.anyAncestor(10) { it == 5 } }
        }
      }

      on("isAncestorOf") {

        it("should return true on its ancestor condition") {
          assertTrue { dependencyTree.isAncestorOf(candidateAncestor = 2, element = 10) }
        }

        it("should return false on not ancestor condition") {
          assertFalse { dependencyTree.isAncestorOf(candidateAncestor = 5, element = 10) }
        }
      }

      on("isNonProjectiveArc") {

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

      on("isNonProjective") {

        it("should return true") {

          assertTrue { dependencyTree.isNonProjective() }
        }
      }

      on("toString") {

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

      on("toString(words)") {

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

      val dependencyTree = DependencyTree(
        size = 12,
        dependencies = listOf(
          ArcConfiguration(0, 2),
          ArcConfiguration(1, 2),
          RootConfiguration(2),
          ArcConfiguration(3, 2),
          ArcConfiguration(4, 6),
          ArcConfiguration(5, 6),
          ArcConfiguration(6, 2),
          ArcConfiguration(7, 8),
          ArcConfiguration(8, 3),
          ArcConfiguration(9, 8),
          ArcConfiguration(10, 11),
          ArcConfiguration(11, 3)
        ))

      on("toString") {

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
})
