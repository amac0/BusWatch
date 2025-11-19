// ABOUTME: Unit tests for LocationRepository location acquisition logic
package com.buswatch.data.repository

import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.buswatch.util.Result
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocationRepositoryTest {

    private lateinit var context: Context
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var repository: LocationRepository

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        locationClient = mockk()
        repository = LocationRepository(context, locationClient)
    }

    @Test
    fun `getCurrentLocation returns success with location`() = runTest {
        val mockLocation = mockk<Location> {
            every { latitude } returns 51.5074
            every { longitude } returns -0.1278
        }

        val taskMock = mockk<Task<Location>>()
        val successSlot = slot<OnSuccessListener<Location>>()

        every { locationClient.lastLocation } returns taskMock
        every { taskMock.addOnSuccessListener(capture(successSlot)) } answers {
            successSlot.captured.onSuccess(mockLocation)
            taskMock
        }
        every { taskMock.addOnFailureListener(any()) } returns taskMock

        val result = repository.getCurrentLocation()

        assertTrue(result is Result.Success)
        val location = (result as Result.Success).data
        assertEquals(51.5074, location.latitude, 0.0001)
    }
}
