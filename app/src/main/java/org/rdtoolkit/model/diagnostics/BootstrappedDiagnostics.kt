package org.rdtoolkit.model.diagnostics

import org.rdtoolkit.model.diagnostics.ConcreteDiagnosticOutcome as cdo
import org.rdtoolkit.model.diagnostics.ConcreteResultProfile as crp

val DIAG_PF_POS = "mal_pf_pos"
val DIAG_PF_NEG = "mal_pf_neg"

val DIAG_PV_POS = "mal_pv_pos"
val DIAG_PV_NEG = "mal_pv_neg"

val DIAG_C19_POS = "sars_cov2_pos"
val DIAG_C19_NEG = "sars_cov2_neg"

val DIAG_PF = "mal_pf"
val DIAG_PV = "mal_pv"

val DIAG_C19 = "sars_cov2"

val RESULT_INDETERMINATE = "indeterminate_result"

val UNIVERSAL_CONTROL_FAILURE = "universal_control_failure"
val CONTROL_VALID = "control_outcome_valid"

fun generateBootstrappedDiagnostics(): MutableMap<String, RdtDiagnosticProfile> {
    var pf_pos = cdo(DIAG_PF_POS, "Pf Positive")
    var pf_neg = cdo(DIAG_PF_NEG, "Pf Negative")

    var control_failure = cdo(UNIVERSAL_CONTROL_FAILURE, "Invalid Test")
    var result_indeterminate = cdo(RESULT_INDETERMINATE, "Indeterminate")

    var pv_pos = cdo(DIAG_PV_POS, "Pv Positive")
    var pv_neg = cdo(DIAG_PV_NEG, "Pv Negative")

    var pf_result = crp(DIAG_PF, "Malaria: P. falciparum", listOf(pf_pos, pf_neg, result_indeterminate, control_failure))
    var pv_result = crp(DIAG_PV, "Malaria: P. vivax", listOf(pv_pos, pv_neg, result_indeterminate, control_failure))

    var s_cov_2_pos = cdo(DIAG_C19_POS, "COVID-19 Positive")
    var s_cov_2_neg = cdo(DIAG_C19_NEG, "COVID-19 Negative")

    var cov_19_result = crp(DIAG_C19, "SARS-CoV-2", listOf(s_cov_2_pos, s_cov_2_neg, control_failure))


    var bioline = ConcreteProfile("sd_bioline_mal_pf_pv", "SD Bioline Malaria Ag Pf/Pv", "sample_bioline",60*15,60*30, listOf(pf_result, pv_result), listOf("real"))
    var standard_q_pf = ConcreteProfile("sd_standard_q_mal_pf_ag", "SD Standard™ Q Malaria P.f Ag", "sample_standard_q_pf",60*15,60*30, listOf(pf_result), listOf("real"))
    var sd_bioline_pf = ConcreteProfile("sd_bioline_mal_pf", "SD Bioline Malaria Ag Pf", "sample_sd_bioline_pf",60*15,60*30, listOf(pf_result), listOf("real"))

    var carestart = ConcreteProfile("carestart_mal_pf_pv", "CareStart™ Malaria Pf/Pv (HRP2/pLDH) Ag Combo RDT", "sample_carestart",60*20,60*30, listOf(pf_result, pv_result), listOf("real"))
    var firstresponse = ConcreteProfile("firstresponse_mal_pf_pv", "First Response® Malaria Ag P.f./P.v. (HRP2/pLDH) Card Test",null, 60*20,60*30, listOf(pf_result, pv_result), listOf("real"))
    var firstresponse_pf = ConcreteProfile("firstresponse_mal_pf", "First Response® Malaria Ag P.f (HRP2) Card Test","sample_firstresponse", 60*20,60*30, listOf(pf_result), listOf("real"))

    var standard_q_c19 = ConcreteProfile("sd_standard_q_c19", "SD STANDARD™ Q COVID-19 Ag Test","sample_std_q_c19", 60*15,60*30, listOf(cov_19_result), listOf("real"))

    var quicktest = ConcreteProfile("debug_mal_pf_pv", "FastResolve Malaria P.f./P.v", null,120,240, listOf(pf_result, pv_result), listOf("fake"))
    var lightnighttest = ConcreteProfile("debug_sf_mal_pf_pv", "LightningQuick Malaria P.f./P.v", null,5,25, listOf(pf_result, pv_result), listOf("fake"))

    var returnSet = HashMap<String, RdtDiagnosticProfile>()

    returnSet.put(bioline.id(), bioline)
    returnSet.put(standard_q_pf.id, standard_q_pf)
    returnSet.put(sd_bioline_pf.id, sd_bioline_pf)
    returnSet.put(carestart.id(), carestart)
    returnSet.put(firstresponse.id(), firstresponse)
    returnSet.put(firstresponse_pf.id(), firstresponse_pf)
    returnSet.put(quicktest.id(), quicktest)
    returnSet.put(lightnighttest.id(), lightnighttest)
    returnSet.put(standard_q_c19.id(), standard_q_c19)

    return returnSet
}