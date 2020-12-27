package com.example.food2you.other

import kotlinx.coroutines.flow.*

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline savedFetchResult: suspend (RequestType) -> Unit,
    crossinline onFetchFailed: (Throwable) -> Unit = { Unit },
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
) = flow {
    emit(Resource.loading(null))
    val data = query().first()

    val flow = if(shouldFetch(data)) {
        emit(Resource.loading(data))

        try {
            val fetchedResult = fetch()
            savedFetchResult(fetchedResult)
            query().map { Resource.success(it) }
        }
        catch (t: Throwable) {
            onFetchFailed(t)
            query().map { Resource.error("Couldn't reach server. It might be down.", it) }
        }
    } else {
        query().map { Resource.success(it) }
    }
    emitAll(flow)
}