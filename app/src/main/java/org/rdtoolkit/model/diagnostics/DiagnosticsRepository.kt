package org.rdtoolkit.model.diagnostics

import org.rdtoolkit.model.session.TestSession
import org.rdtoolkit.model.session.TestSessionDao
import java.util.HashMap

class DiagnosticsRepository() {
    var builtInSources : MutableMap<String, RdtDiagnosticProfile> = generateBootstrappedDiagnostics()

    var sourcesByTag : List<Pair<RdtDiagnosticProfile, Set<String>>> = populateTags()

    fun getTestProfile(id: String) : RdtDiagnosticProfile {
        var profile = builtInSources.get(id) ?: throw Exception("No internal profile for: " + id)
        return profile
    }

    fun getMatchingTestProfiles(tagSet: Set<String>, orOp : Boolean) : Collection<RdtDiagnosticProfile> {
        val returnSet = ArrayList<RdtDiagnosticProfile>()
        for (source in sourcesByTag) {
            if (orOp) {
                for (tag in tagSet) {
                    if(source.second.contains(tag)) {
                        returnSet.add(source.first)
                        continue
                    }
                }
            } else {
                if(source.second.containsAll(tagSet)) {
                    returnSet.add(source.first)
                    continue
                }
            }
        }
        return returnSet
    }


    private fun populateTags() : List<Pair<RdtDiagnosticProfile, Set<String>>> {
        val returnSet = ArrayList<Pair<RdtDiagnosticProfile, Set<String>>>()
        for (profile in builtInSources.values) {
            val tags = HashSet<String>()
            tags.add(profile.id())
            for (resultProfile in profile.resultProfiles()) {
                tags.add(resultProfile.id())
            }
            tags.addAll(profile.tags())

            returnSet.add(Pair(profile, tags))
        }
        return returnSet
    }

    fun getInstructionSetsForTestProfile(profileId : String) : List<InstructionsSet> {
        return ArrayList()
    }

    init {

    }
}