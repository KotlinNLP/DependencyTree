/* Copyright 2017-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.dependencytree

/**
 * Error raised when a cycle is detected in a dependency tree.
 */
class CycleDetectedError : RuntimeException()

/**
 * Error raised when an invalid Arc is managed.
 *
 * @param dependent the dependent of the arc
 * @param governor the governor of the arc
 */
class InvalidArc(dependent: Int, governor: Int) : RuntimeException("$dependent <- $governor")
