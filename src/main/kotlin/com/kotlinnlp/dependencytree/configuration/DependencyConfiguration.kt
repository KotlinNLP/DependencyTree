/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.dependencytree.configuration

import com.kotlinnlp.linguisticdescription.DependencyRelation

/**
 * The configuration of a dependency.
 */
interface DependencyConfiguration {

  /**
   * The dependency relation (can be null).
   */
  val dependencyRelation: DependencyRelation?

  /**
   * The attachment score.
   */
  val attachmentScore: Double
}
