/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.dependencytree.configuration

/**
 * The configuration of an arc of the DependencyTree.
 *
 * @property dependent
 * @property governor an element of the tree
 * @property attachmentScore the attachment score
 */
interface ArcConfiguration {

  /**
   * The dependent tree element.
   */
  val dependent: Int

  /**
   * The governor tree element.
   */
  val governor: Int

  /**
   * The attachment score.
   */
  val attachmentScore: Double
}
