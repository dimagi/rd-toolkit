package org.rdtoolkit.model.diagnostics

interface RdtDiagnosticProfile {
    fun id() : String
    fun readableName() : String
    fun timeToResolve() : Int
    fun timeToExpire() : Int
    fun resultProfiles() : Collection<ResultProfile>
}

interface DiagnosticOutcome {
    fun id(): String
    fun readableName(): String
}

interface ResultProfile {
    fun id() : String
    fun readableName() : String
    fun outcomes() : Collection<DiagnosticOutcome>
}

data class ConcreteDiagnosticOutcome ( var id : String,
                                       var readableName: String
) : DiagnosticOutcome {
    override fun id(): String {
        return id;
    }

    override fun readableName(): String {
        return readableName;
    }
}

data class ConcreteResultProfile (  var id : String,
                                    var readableName: String,
                                    var resultProfiles: Collection<DiagnosticOutcome>
) : ResultProfile {
    override fun id(): String {
        return id;
    }

    override fun readableName(): String {
        return readableName;
    }

    override fun outcomes(): Collection<DiagnosticOutcome> {
        return resultProfiles
    }

}

data class ConcreteProfile ( var id : String,
                             var readableName: String,
                             var timeToResolve: Int,
                             var timeToExpire: Int,
                             var resultProfiles: Collection<ResultProfile>

) : RdtDiagnosticProfile {
    override fun id(): String {
        return id;
    }

    override fun readableName(): String {
        return readableName;
    }

    override fun timeToResolve(): Int {
        return timeToResolve
    }

    override fun timeToExpire(): Int {
        return timeToExpire
    }

    override fun resultProfiles(): Collection<ResultProfile> {
        return resultProfiles
    }
}