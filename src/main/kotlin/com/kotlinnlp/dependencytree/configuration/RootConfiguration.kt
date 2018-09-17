/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.dependencytree.configuration

import com.kotlinnlp.linguisticdescription.GrammaticalConfiguration
import com.kotlinnlp.linguisticdescription.Deprel

/**
 * The configuration of the root.
 *
 * @property id an element of the tree
 * @property grammaticalConfiguration the grammatical configuration of the root of the tree (can be null)
 * @property attachmentScore the attachment score (default = 0.0)
 */
data class RootConfiguration(
  val id: Int,
  override val grammaticalConfiguration: GrammaticalConfiguration? = null,
  override val attachmentScore: Double = 0.0
) : DependencyConfiguration {

  /**
   * Build a [RootConfiguration] given a [Deprel] only instead of a complete [GrammaticalConfiguration].
   *
   * @param id an element of the tree
   * @param deprel the deprel (can be null)
   * @param attachmentScore the attachment score (default = 0.0)
   */
  constructor(id: Int, deprel: Deprel?, attachmentScore: Double = 0.0): this(
    id = id,
    grammaticalConfiguration = deprel?.let { GrammaticalConfiguration(deprel = it) },
    attachmentScore = attachmentScore
  )
}
