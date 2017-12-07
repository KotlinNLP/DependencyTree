/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.dependencytree

import java.io.Serializable

/**
 * The Part-Of-Speech (POS) tag.
 *
 * @property label the label of this POS
 */
data class POSTag(val label: String): Serializable {

  companion object {

    /**
     * Private val used to serialize the class (needed from Serializable)
     */
    @Suppress("unused")
    private const val serialVersionUID: Long = 1L
  }

  /**
   * @return a string representation of this POS tag
   */
  override fun toString(): String = this.label

  /**
   * @return a Boolean indicating whether the given [other] object is equal to this [POSTag]
   */
  override fun equals(other: Any?): Boolean
    = other is POSTag && other.label == this.label

  /**
   * @return the hash code of this [POSTag]
   */
  override fun hashCode(): Int = this.label.hashCode()
}
