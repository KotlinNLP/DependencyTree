/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.dependencytree.configuration

import com.kotlinnlp.linguisticdescription.GrammaticalConfiguration
import com.kotlinnlp.linguisticdescription.GrammaticalConfiguration.Component
import com.kotlinnlp.linguisticdescription.syntax.SyntacticDependency

/**
 * The configuration of an arc.
 *
 * @property dependent an element of the tree
 * @property governor an element of the tree
 * @property grammaticalConfiguration the grammatical configuration between the [dependent] and the [governor] (can be null)
 * @property attachmentScore the attachment score (default = 0.0)
 */
data class ArcConfiguration(
  val dependent: Int,
  val governor: Int,
  override val grammaticalConfiguration: GrammaticalConfiguration? = null,
  override val attachmentScore: Double = 0.0
) : DependencyConfiguration {

  /**
   * Build a [RootConfiguration] given a [SyntacticDependency] only instead of a complete [GrammaticalConfiguration].
   *
   * @param dependent an element of the tree
   * @param governor an element of the tree
   * @param dependency the syntactic dependency (can be null)
   * @param attachmentScore the attachment score (default = 0.0)
   */
  constructor(
    dependent: Int,
    governor: Int,
    dependency: SyntacticDependency?,
    attachmentScore: Double = 0.0
  ): this(
    dependent = dependent,
    governor = governor,
    grammaticalConfiguration = dependency?.let { GrammaticalConfiguration(Component(it)) },
    attachmentScore = attachmentScore
  )
}
