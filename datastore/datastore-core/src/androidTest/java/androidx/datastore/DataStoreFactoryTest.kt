/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.datastore

import android.content.Context
import androidx.datastore.handlers.ReplaceFileCorruptionHandler
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@kotlinx.coroutines.ExperimentalCoroutinesApi
@kotlinx.coroutines.ObsoleteCoroutinesApi
@kotlinx.coroutines.FlowPreview
class DataStoreFactoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var testFile: File
    private lateinit var dataStoreScope: TestCoroutineScope
    private lateinit var context: Context

    @Before
    fun setUp() {
        testFile = tmp.newFile()
        dataStoreScope = TestCoroutineScope()
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testNewInstance() = runBlockingTest {
        val store = DataStoreFactory.create(
            produceFile = { testFile },
            serializer = TestingSerializer(),
            scope = dataStoreScope
        )

        val expectedByte = 123.toByte()

        assertThat(
            store.updateData {
                expectedByte
            }
        ).isEqualTo(expectedByte)
        assertThat(store.data.first()).isEqualTo(expectedByte)
    }

    @Test
    fun testCorruptionHandlerInstalled() = runBlockingTest {
        val valueToReplace = 123.toByte()

        val store = DataStoreFactory.create(
            produceFile = { testFile },
            serializer = TestingSerializer(failReadWithCorruptionException = true),
            corruptionHandler = ReplaceFileCorruptionHandler<Byte> {
                valueToReplace
            },
            scope = dataStoreScope
        )

        assertThat(store.data.first()).isEqualTo(valueToReplace)
    }

    @Test
    fun testMigrationsInstalled() = runBlockingTest {
        val migratedByte = 1

        val migratePlus2 = object : DataMigration<Byte> {
            override suspend fun shouldMigrate(currentData: Byte) = true
            override suspend fun migrate(currentData: Byte) = currentData.inc().inc()
            override suspend fun cleanUp() {}
        }
        val migrateMinus1 = object : DataMigration<Byte> {
            override suspend fun shouldMigrate(currentData: Byte) = true

            override suspend fun migrate(currentData: Byte) = currentData.dec()

            override suspend fun cleanUp() {}
        }

        val store = DataStoreFactory.create(
            produceFile = { testFile },
            migrations = listOf(migratePlus2, migrateMinus1),
            scope = dataStoreScope,
            serializer = TestingSerializer()
        )

        assertThat(store.data.first()).isEqualTo(migratedByte)
    }

    @Test
    fun testCreateWithContextAndName() = runBlockingTest {
        val byte = 1

        var store = context.createDataStore(
            serializer = TestingSerializer(),
            fileName = "my_settings.byte",
            scope = dataStoreScope
        )
        store.updateData { 1 }

        // Create it again and confirm it's still there
        store = context.createDataStore(
            serializer = TestingSerializer(),
            fileName = "my_settings.byte",
            scope = dataStoreScope
        )
        assertThat(store.data.first()).isEqualTo(byte)

        // Check that the file name is context.filesDir + fileName
        store = DataStoreFactory.create(
            produceFile = {
                File(context.filesDir, "datastore/my_settings.byte")
            },
            serializer = TestingSerializer(),
            scope = dataStoreScope
        )
        assertThat(store.data.first()).isEqualTo(byte)
    }
}