package org.rdtoolkit.model.diagnostics

import org.rdtoolkit.model.diagnostics.ConcreteDiagnosticOutcome as cdo
import org.rdtoolkit.model.diagnostics.ConcreteResultProfile as crp

fun generateBootstrappedDiagnostics(): MutableMap<String, RdtDiagnosticProfile> {
    var pf_pos = cdo("mal_pf_pos", "Pf Positive")
    var pf_neg = cdo("mal_pf_neg", "Pf Negative")

    var control_failure = cdo("universal_control_failure", "Control Failed - No Result")

    var pv_pos = cdo("mal_pv_pos", "Pv Positive")
    var pv_neg = cdo("mal_pv_neg", "Pv Negative")

    var pf_result = crp("mal_pf", "Malaria: P. falciparum", listOf(pf_pos, pf_neg, control_failure))
    var pv_result = crp("mal_pv", "Malaria: P. vivax", listOf(pv_pos, pv_neg, control_failure))


    var bioline = ConcreteProfile("sd_bioline_mal_pf_pv", "SD Bioline Malaria Ag Pf/Pv", "sample_bioline",60*15,60*30, listOf(pf_result, pv_result), listOf("real"))
    var standard_q_pf = ConcreteProfile("sd_standard_q_mal_pf_ag", "SD Standard Q Malaria P.f Ag", "sample_standard_q_pf",60*15,60*30, listOf(pf_result), listOf("real"))

    var carestart = ConcreteProfile("carestart_mal_pf_pv", "CareStart™ Malaria Pf/Pv (HRP2/pLDH) Ag Combo RDT", "sample_carestart",60*20,60*30, listOf(pf_result, pv_result), listOf("real"))
    var firstresponse = ConcreteProfile("firstresponse_mal_pf_pv", "First Response® Malaria Ag P.f./P.v. (HRP2/pLDH) Card Test",null, 60*20,60*30, listOf(pf_result, pv_result), listOf("real"))
    var firstresponse_pf = ConcreteProfile("firstresponse_mal_pf", "First Response® Malaria Ag P.f (HRP2) Card Test","sample_firstresponse", 60*20,60*30, listOf(pf_result), listOf("real"))

    var quicktest = ConcreteProfile("debug_mal_pf_pv", "FastResolve Malaria P.f./P.v", null,120,240, listOf(pf_result, pv_result), listOf("fake"))
    var lightnighttest = ConcreteProfile("debug_sf_mal_pf_pv", "LightningQuick Malaria P.f./P.v", null,5,25, listOf(pf_result, pv_result), listOf("fake"))

    var returnSet = HashMap<String, RdtDiagnosticProfile>()

    returnSet.put(bioline.id(), bioline)
    returnSet.put(standard_q_pf.id, standard_q_pf)
    returnSet.put(carestart.id(), carestart)
    returnSet.put(firstresponse.id(), firstresponse)
    returnSet.put(firstresponse_pf.id(), firstresponse_pf)
    returnSet.put(quicktest.id(), quicktest)
    returnSet.put(lightnighttest.id(), lightnighttest)

    return returnSet
}