/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.dependencytree

/**
 * RootConfiguration
 *
 * @property id an element of the tree
 * @property deprel the dependency relation of the top of the tree (can be null)
 * @property attachmentScore the attachment score (default = 0.0)
 */
data class RootConfiguration(
  val id: Int,
  override val deprel: Deprel? = null,
  override val attachmentScore: Double = 0.0
) : DependencyConfiguration
