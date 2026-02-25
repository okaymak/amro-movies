package com.amro.movies.di.bindings

import com.amro.movies.data.repository.TmdbMovieRepository
import com.amro.movies.domain.repository.MovieRepository
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds

@BindingContainer
interface RepositoryBindings {
    @Binds
    fun bindMovieRepository(repository: TmdbMovieRepository): MovieRepository
}