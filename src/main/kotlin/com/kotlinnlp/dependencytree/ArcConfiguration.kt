/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.dependencytree

/**
 * ArcConfiguration.
 *
 * @property dependent an element of the tree
 * @property governor an element of the tree
 * @property deprel the syntactic relation that connects the [dependent] with the [governor] (can be null)
 * @property attachmentScore the attachment score (default = 0.0)
 */
data class ArcConfiguration(
  val dependent: Int,
  val governor: Int,
  override val deprel: Deprel? = null,
  override val attachmentScore: Double = 0.0
) : DependencyConfiguration
