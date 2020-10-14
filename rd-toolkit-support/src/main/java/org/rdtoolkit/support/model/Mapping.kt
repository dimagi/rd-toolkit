package org.rdtoolkit.support.model

interface Mapper<I, O> {
    fun map(input: I): O
}
class ChainingMapper<I, O, T>(
        private val mapper: Mapper<I, O>,
        private val extension: Mapper<O, T>
) : Mapper<I, T> {
    override fun map(input: I): T {
        return extension.map(mapper.map(input))
    }
}


fun <I,O,T> Mapper<I, O>.chain(extension : Mapper<O, T>) : Mapper<I, T> {
    return ChainingMapper(this, extension)
}


// Non-nullable to Non-nullable
interface ListMapper<I, O>: Mapper<List<I>, List<O>>

class ListMapperImpl<I, O>(
        private val mapper: Mapper<I, O>
) : ListMapper<I, O> {
    override fun map(input: List<I>): List<O> {
        return input.map { mapper.map(it) }
    }
}