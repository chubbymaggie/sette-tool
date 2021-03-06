/*
 * SETTE - Symbolic Execution based Test Tool Evaluator
 *
 * SETTE is a tool to help the evaluation and comparison of symbolic execution
 * based test input generator tools.
 *
 * Budapest University of Technology and Economics (BME)
 *
 * Authors: Lajos Cseppentő <lajos.cseppento@inf.mit.bme.hu>, Zoltán Micskei
 * <micskeiz@mit.bme.hu>
 *
 * Copyright 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package hu.bme.mit.sette.common.validator.exceptions;

import java.io.File;

/**
 * Exception class for file validation errors.
 */
public final class FileValidationException extends ValidationException {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6298967596971525691L;

    /** The file. */
    private final File file;

    /**
     * Instantiates a new file validation exception.
     *
     * @param message
     *            the message
     * @param pFile
     *            the file
     */
    public FileValidationException(final String message,
            final File pFile) {
        this(message, pFile, null);
    }

    /**
     * Instantiates a new file validation exception.
     *
     * @param message
     *            the message
     * @param pFile
     *            the file
     * @param cause
     *            the cause
     */
    public FileValidationException(final String message,
            final File pFile, final Throwable cause) {
        super(message + String.format("\n(file: [%s])", pFile), cause);
        file = pFile;
    }

    /**
     * Gets the file.
     *
     * @return the file
     */
    public File getFile() {
        return file;
    }
}
