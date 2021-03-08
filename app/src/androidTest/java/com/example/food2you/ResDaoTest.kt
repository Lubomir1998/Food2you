package com.example.food2you

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.food2you.data.local.DbHelper
import com.example.food2you.data.local.ResDao
import com.example.food2you.data.remote.models.Order
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
@ExperimentalCoroutinesApi
class ResDaoTest {

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: DbHelper
    private lateinit var dao: ResDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DbHelper::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = database.getDao()
    }

    @After
    fun teardown() {
        database.close()
    }


    @Test
    fun checkIfInsertingOrdersWork() = runBlockingTest {

        val order = Order(
            "restaurant@mail.bg",
            "street 3",
            "recipient token",
            "mail@abv.com",
            "088123456",
            listOf(),
            23.20f,
            87342497L,
            "STATUS_WAITING",
            "www.imgurl.com",
            "Riviera"
        )

        dao.insertOrder(order)

        val orders = dao.getAllOrdersForUser().asLiveData().getOrAwaitValue()

        assertThat(orders).contains(order)


    }


    @Test
    fun checkIfDeletingAllOrdersWork() = runBlockingTest {
        val order1 = Order(
            "restaurant@mail.bg",
            "street 3",
            "recipient token",
            "mail@abv.com",
            "088123456",
            listOf(),
            23.20f,
            87342497L,
            "STATUS_WAITING",
            "www.imgurl.com",
            "Riviera",
            id = "abv"
        )

        val order2 = Order(
            "restaurant@mail.bg",
            "street 3",
            "recipient token",
            "mail@abv.com",
            "088123456",
            listOf(),
            23.20f,
            87342497L,
            "STATUS_WAITING",
            "www.imgurl.com",
            "Riviera",
            id = "abvd"
        )

        dao.insertOrder(order1)
        dao.insertOrder(order2)

        dao.deleteAllOrders()

        val orders = dao.getAllOrdersForUser().asLiveData().getOrAwaitValue()

        assertThat(orders).isEmpty()

    }





}