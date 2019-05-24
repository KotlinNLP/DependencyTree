/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.dependencytree.configuration

import com.kotlinnlp.linguisticdescription.GrammaticalConfiguration
import com.kotlinnlp.linguisticdescription.syntax.SyntacticDependency

/**
 * The configuration of a dependency of the DependencyTree.
 */
sealed class DependencyConfiguration {

  /**
   * The attachment score.
   */
  abstract val attachmentScore: Double

  /**
   * The configuration of an unlabeled dependency.
   */
  sealed class Unlabeled : DependencyConfiguration() {

    /**
     * The configuration of an unlabeled arc.
     *
     * @property dependent the dependent tree element
     * @property governor the governor tree element
     * @property attachmentScore the attachment score (default = 0.0)
     */
    data class Arc(
      override val dependent: Int,
      override val governor: Int,
      override val attachmentScore: Double = 0.0
    ) : ArcConfiguration, DependencyConfiguration.Unlabeled()

    /**
     * The configuration of an unlabeled root.
     *
     * @property id the root id
     * @property attachmentScore the attachment score (default = 0.0)
     */
    data class Root(
      override val id: Int,
      override val attachmentScore: Double = 0.0
    ) : RootConfiguration, DependencyConfiguration.Unlabeled()
  }

  /**
   * The configuration of a labeled dependency.
   */
  sealed class Labeled : DependencyConfiguration() {

    /**
     * The grammatical configuration.
     */
    abstract val grammaticalConfiguration: GrammaticalConfiguration

    /**
     * The configuration of a labeled arc.
     *
     * @property dependent the dependent tree element
     * @property governor the governor tree element
     * @property grammaticalConfiguration the grammatical configuration between the [dependent] and the [governor]
     * @property attachmentScore the attachment score (default = 0.0)
     */
    data class Arc(
      override val dependent: Int,
      override val governor: Int,
      override val grammaticalConfiguration: GrammaticalConfiguration,
      override val attachmentScore: Double = 0.0
    ) : ArcConfiguration, DependencyConfiguration.Labeled() {

      /**
       * Build an Arc configuration given a [SyntacticDependency] only instead of a complete [GrammaticalConfiguration].
       *
       * @param dependent an element of the tree
       * @param governor an element of the tree
       * @param dependency the syntactic dependency
       * @param attachmentScore the attachment score (default = 0.0)
       */
      constructor(
        dependent: Int,
        governor: Int,
        dependency: SyntacticDependency,
        attachmentScore: Double = 0.0
      ): this(
        dependent = dependent,
        governor = governor,
        grammaticalConfiguration = GrammaticalConfiguration(GrammaticalConfiguration.Component(dependency)),
        attachmentScore = attachmentScore
      )
    }

    /**
     * The configuration of a labeled root.
     *
     * @property id the root id
     * @property grammaticalConfiguration the grammatical configuration of the root of the tree (can be null)
     * @property attachmentScore the attachment score (default = 0.0)
     */
    data class Root(
      override val id: Int,
      override val grammaticalConfiguration: GrammaticalConfiguration,
      override val attachmentScore: Double = 0.0
    ) : RootConfiguration, DependencyConfiguration.Labeled() {

      /**
       * Build a Root configuration given a [SyntacticDependency] only instead of a complete [GrammaticalConfiguration].
       *
       * @param id an element of the tree
       * @param dependency the syntactic dependency
       * @param attachmentScore the attachment score (default = 0.0)
       */
      constructor(id: Int, dependency: SyntacticDependency, attachmentScore: Double = 0.0): this(
        id = id,
        grammaticalConfiguration = GrammaticalConfiguration(GrammaticalConfiguration.Component(dependency)),
        attachmentScore = attachmentScore
      )
    }
  }
}
