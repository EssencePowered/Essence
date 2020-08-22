/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.storage.exceptions;

public class DataDeleteException extends Exception {

    public DataDeleteException(final String mesage, final Exception innerException) {
        super(mesage, innerException);
    }
}
