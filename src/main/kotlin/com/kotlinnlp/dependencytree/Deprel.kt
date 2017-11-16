/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.dependencytree

import java.io.Serializable

/**
 * The Deprel.
 *
 * @property label a name for the dependency relation
 * @property direction the direction in a possible syntactic context for which this deprel is valid
 */
open class Deprel(val label: String, val direction: Position = Position.NULL) : Serializable {

  companion object {

    /**
     * Private val used to serialize the class (needed from Serializable)
     */
    @Suppress("unused")
    private const val serialVersionUID: Long = 1L
  }

  /**
   * Deprel Position.
   */
  enum class Position {
    ROOT,
    LEFT,
    RIGHT,
    NULL
  }

  /**
   * @return the string representation of this [Deprel]
   */
  override fun toString(): String = this.label + ":" + this.direction

  /**
   * @return a Boolean indicating whether the given [other] object is equal to this [Deprel]
   */
  override fun equals(other: Any?): Boolean {
    return if (other is Deprel){
      other.label == this.label && other.direction == this.direction
    } else {
      false
    }
  }

  /**
   * @return the hash code of this [Deprel]
   */
  override fun hashCode(): Int {
    return this.label.hashCode() * 31 + this.direction.hashCode()
  }
}
