/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

import com.kotlinnlp.dependencytree.ArcConfiguration
import com.kotlinnlp.dependencytree.DependencyTree
import com.kotlinnlp.dependencytree.RootConfiguration
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

      on("containsCycle") {

        it("should return false") {
          assertFalse { dependencyTree.containsCycle() }
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
     * head  |  -1    2    4    4    1
     */
    context("pre-initialized with a cycle") {

      val dependencyTree = DependencyTree(
        size = 5,
        dependencies = listOf(
          ArcConfiguration(1, 2),
          ArcConfiguration(2, 4),
          ArcConfiguration(3, 4),
          ArcConfiguration(4, 1)
        ),
        allowCycles = true)

      on("containsCycle") {

        it("should return true") {
          assertTrue { dependencyTree.containsCycle() }
        }
      }
    }

    /**
     * id    |   0    1    2    3    4
     * head  |  -1    0    0    4    0
     */
    context("pre-initialized with a single root") {

      val dependencyTree = DependencyTree(size = 5, dependencies = listOf(
        ArcConfiguration(1, 0),
        ArcConfiguration(2, 0),
        ArcConfiguration(3, 4),
        ArcConfiguration(4, 0)
      ))

      it("should have the expected root") {
        assertEquals(listOf(0), dependencyTree.roots)
      }

      on("hasSingleRoot") {

        it("should return true"){
          assertTrue { dependencyTree.hasSingleRoot() }
        }
      }

      on("containsCycle") {

        it("should return false") {
          assertFalse { dependencyTree.containsCycle() }
        }
      }

      on("checkCycleWith") {

        it("should detect the introduction of a cycle") {
          assertTrue { dependencyTree.checkCycleWith(dependent = 4, governor = 3) }
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
     * id    |   0    1    2    3    4
     * head  |  -1    0    1    4   -1
     */
    context("pre-initialized with two roots") {

      val dependencyTree = DependencyTree(size = 5, dependencies = listOf(
        ArcConfiguration(1, 0),
        ArcConfiguration(2, 1),
        ArcConfiguration(3, 4)
      ))

      it("should have the expected root") {
        assertEquals(listOf(0, 4), dependencyTree.roots)
      }

      on("hasSingleRoot") {

        it("should return true"){
          assertFalse { dependencyTree.hasSingleRoot() }
        }
      }

      on("containsCycle") {

        it("should return false") {
          assertFalse { dependencyTree.containsCycle() }
        }
      }

      on("checkCycleWith") {

        it("should detect the feasibility of an arc") {
          assertFalse { dependencyTree.checkCycleWith(dependent = 4, governor = 2) }
        }

        it("should detect the introduction of a cycle ") {
          assertTrue { dependencyTree.checkCycleWith(dependent = 0, governor = 2) }
        }
      }

      on("toString") {

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

      val dependencyTree = DependencyTree(size = 12, dependencies = listOf(
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

      on("inOrder") {

        it("should return the expected list") {
          assertEquals(listOf(0, 1, 2, 3, 7, 8, 9, 10, 11, 4, 5, 6), dependencyTree.inOrder())
        }
      }

      on("projectiveOrder") {

        it("should return the expected list") {
          assertEquals(listOf(0, 1, 2, 3, 9, 10, 11, 4, 5, 6, 7, 8), dependencyTree.projectiveOrder())
        }
      }

      on("containsCycle") {

        it("should return false") {
          assertFalse { dependencyTree.containsCycle() }
        }
      }

      on("inNonProjectiveArcArc") {

        it("should return false at index 0") {
          assertFalse(dependencyTree.inNonProjectiveArc(0))
        }

        it("should return false at index 1") {
          assertFalse(dependencyTree.inNonProjectiveArc(1))
        }

        it("should return false at index 2") {
          assertFalse(dependencyTree.inNonProjectiveArc(2))
        }

        it("should return false at index 3") {
          assertFalse(dependencyTree.inNonProjectiveArc(3))
        }

        it("should return false at index 4") {
          assertFalse(dependencyTree.inNonProjectiveArc(4))
        }

        it("should return false at index 5") {
          assertFalse(dependencyTree.inNonProjectiveArc(5))
        }

        it("should return false at index 6") {
          assertFalse(dependencyTree.inNonProjectiveArc(6))
        }

        it("should return false at index 7") {
          assertFalse(dependencyTree.inNonProjectiveArc(7))
        }

        it("should return false at index 8") {
          assertTrue(dependencyTree.inNonProjectiveArc(8))
        }

        it("should return false at index 9") {
          assertFalse(dependencyTree.inNonProjectiveArc(9))
        }

        it("should return false at index 10") {
          assertFalse(dependencyTree.inNonProjectiveArc(10))
        }

        it("should return false at index 11") {
          assertFalse(dependencyTree.inNonProjectiveArc(11))
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
     * head  |  2    2    -1    2     6   6   2   8   3   8    11     8
     */
    context("pre-initialized with overlapping 'continue' string paddings") {

      val dependencyTree = DependencyTree(size = 12, dependencies = listOf(
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

      on("toString(words)") {

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
