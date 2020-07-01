package tr.com.srdc.cda2fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Condition;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.consol.Indication;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.generator.IndicationGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.util.FHIRUtil;

public class ConditionTest {

	private static CDAFactories factories;
	private static IndicationGenerator indicationGenerator;
	private static ResourceTransformerImpl rt = new ResourceTransformerImpl();

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		factories = CDAFactories.init();
		indicationGenerator = IndicationGenerator.getDefaultInstance();
	}

	@Test
	public void tIndication2ConditionCategoryTest() {
		Indication indication = indicationGenerator.generate(factories);

		IEntryResult encounterCondResult = rt.tIndication2ConditionEncounter(indication, new BundleInfo(rt));
		Condition encounterCondition = null;
		if (encounterCondResult.hasResult()) {
			Bundle bundle = encounterCondResult.getBundle();
			encounterCondition = FHIRUtil.findFirstResource(bundle, Condition.class);
		} else {
			Bundle bundle = encounterCondResult.getFullBundle();
			encounterCondition = FHIRUtil.findFirstResource(bundle, Condition.class);
		}

		IEntryResult problemListItemResult = rt.tIndication2ConditionProblemListItem(indication, new BundleInfo(rt));
		Condition problemListItemCondition = null;
		if (problemListItemResult.hasResult()) {
			Bundle bundle = problemListItemResult.getBundle();
			problemListItemCondition = FHIRUtil.findFirstResource(bundle, Condition.class);
		} else {
			Bundle bundle = problemListItemResult.getFullBundle();
			problemListItemCondition = FHIRUtil.findFirstResource(bundle, Condition.class);
		}

		String categoryDisplay = encounterCondition.getCategoryFirstRep().getCodingFirstRep().getDisplay();
		String categoryCode = encounterCondition.getCategoryFirstRep().getCodingFirstRep().getCode();
		String categorySystem = encounterCondition.getCategoryFirstRep().getCodingFirstRep().getSystem();

		Assert.assertEquals("category system is http://hl7.org/fhir/condition-category",
				"http://hl7.org/fhir/condition-category", categorySystem);
		Assert.assertEquals("category code is encounter-diagnosis", "encounter-diagnosis", categoryCode);
		Assert.assertEquals("category displauy is Encounter Diagnosis", "Encounter Diagnosis", categoryDisplay);

		categoryDisplay = problemListItemCondition.getCategoryFirstRep().getCodingFirstRep().getDisplay();
		categoryCode = problemListItemCondition.getCategoryFirstRep().getCodingFirstRep().getCode();
		categorySystem = problemListItemCondition.getCategoryFirstRep().getCodingFirstRep().getSystem();

		Assert.assertEquals("category system is http://hl7.org/fhir/condition-category",
				"http://hl7.org/fhir/condition-category", categorySystem);
		Assert.assertEquals("category code is problem-list-item", "problem-list-item", categoryCode);
		Assert.assertEquals("category displauy is Problem List Item", "Problem List Item", categoryDisplay);

	}

	@Test
	public void testRepeatConditionCategory() {
		BundleInfo bundleInfo = new BundleInfo(rt);

		Indication indication = indicationGenerator.generate(factories);

		IEntryResult encounterCondResult = rt.tIndication2ConditionEncounter(indication, bundleInfo);

		bundleInfo.updateFrom(encounterCondResult);

		IEntryResult problemListItemResult = rt.tIndication2ConditionProblemListItem(indication, bundleInfo);
		Condition problemListItemCondition = null;

		if (problemListItemResult.hasResult()) {
			Bundle bundle = problemListItemResult.getBundle();
			problemListItemCondition = FHIRUtil.findFirstResource(bundle, Condition.class);
		} else {
			Bundle bundle = problemListItemResult.getFullBundle();
			problemListItemCondition = FHIRUtil.findFirstResource(bundle, Condition.class);
		}
		CodeableConcept categoryEncounter = problemListItemCondition.getCategoryFirstRep();
		CodeableConcept categoryProblemListItem = problemListItemCondition.getCategory().get(1);

		Assert.assertEquals("encounter-diagnosis", categoryEncounter.getCodingFirstRep().getCode());
		Assert.assertEquals("problem-list-item", categoryProblemListItem.getCodingFirstRep().getCode());

	}

}
