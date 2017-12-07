/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.dependencytree

/**
 * The Part-Of-Speech (POS) tag.
 *
 * @property label the label of this POS
 */
data class POSTag(val label: String){

  /**
   * @return a string representation of this pos tag
   */
  override fun toString(): String = this.label
}