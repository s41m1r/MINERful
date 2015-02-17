package minerful.miner.engine;

import java.util.SortedSet;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.AlternateResponse;
import minerful.concept.constraint.relation.AlternateSuccession;
import minerful.concept.constraint.relation.ChainPrecedence;
import minerful.concept.constraint.relation.ChainResponse;
import minerful.concept.constraint.relation.ChainSuccession;
import minerful.concept.constraint.relation.CoExistence;
import minerful.concept.constraint.relation.Precedence;
import minerful.concept.constraint.relation.RespondedExistence;
import minerful.concept.constraint.relation.Response;
import minerful.concept.constraint.relation.Succession;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.LocalStatsWrapper;
import minerful.miner.stats.LocalStatsWrapperForCharsets;
import minerful.miner.stats.charsets.CharactersSetCounter;

public class ProbabilisticRelationOutBranchedConstraintsMiningEngine {
	private GlobalStatsTable globalStats;

	public ProbabilisticRelationOutBranchedConstraintsMiningEngine(
			GlobalStatsTable globalStats) {
		this.globalStats = globalStats;
	}

	public AlternatePrecedence discoverBranchedAlternatePrecedenceConstraints(
			TaskChar pivotTaskCh,
			TaskCharSet comboToAnalyze) {
		AlternatePrecedence nuConstraint = null;
	
		int	negativeOccurrences = 0,
			denominator = 0;
		double support = 0;
		LocalStatsWrapper searchedStatsWrapper = null;
	
		for (Character searched : comboToAnalyze.getListOfIdentifiers()) {
			searchedStatsWrapper = globalStats.statsTable.get(searched);
			negativeOccurrences += searchedStatsWrapper.localStatsTable.get(pivotTaskCh.identifier).howManyTimesItNeverAppearedBackwards();
			negativeOccurrences += searchedStatsWrapper.localStatsTable.get(pivotTaskCh.identifier).betweenBackwards;
			denominator += searchedStatsWrapper.getTotalAmountOfAppearances();
		}

		support = 1.0 - (double) negativeOccurrences / (double) denominator;
		nuConstraint = new AlternatePrecedence(
				new TaskCharSet(pivotTaskCh),
				comboToAnalyze,
				support);
	
		return nuConstraint;
	}

	public AlternateResponse discoverBranchedAlternateResponseConstraints(
			TaskChar pivotTaskCh,
			LocalStatsWrapper pivotLocalStats,
			long pivotAppearances,
			TaskCharSet comboToAnalyze) {
		AlternateResponse nuConstraint = null;
		if (pivotAppearances < 1)
			return nuConstraint;

		double support = 0;
		int negativeOccurrences = 0;

		LocalStatsWrapperForCharsets extPivotLocalStats = (LocalStatsWrapperForCharsets) pivotLocalStats;
		SortedSet<CharactersSetCounter> neverAppearedCharSets = 
				extPivotLocalStats.getNeverMoreAppearedAfterCharacterSets()
				.selectCharSetCountersSharedAmong(
						comboToAnalyze.getListOfIdentifiers()
				);
		SortedSet<CharactersSetCounter> alternationCharSets = 
				extPivotLocalStats.getRepetitionsBeforeCharactersAppearingAfter()
				.selectCharSetCountersSharedAmong(
						comboToAnalyze.getListOfIdentifiers()
				);
//		CharactersSetCounter alternationsCounter =
//				extPivotLocalStats
//				.getRepetitionsBeforeCharactersAppearingAfter().getNearest(
//						comboToAnalyze.getListOfIdentifiers()
//				);
		for (CharactersSetCounter neverAppearedAfterCharSet : neverAppearedCharSets) {
			negativeOccurrences += neverAppearedAfterCharSet.getCounter();
		}
//		if (alternationsCounter != null)
//			negativeOccurrences += alternationsCounter.getCounter();
		for (CharactersSetCounter alternationAfterCharSet : alternationCharSets) {
			negativeOccurrences += alternationAfterCharSet.getCounter();
		}
		
		support = 1.0 - (double)negativeOccurrences  / (double)pivotAppearances;
		nuConstraint = new AlternateResponse(
				new TaskCharSet(pivotTaskCh),
				comboToAnalyze,
				support);
	
		return nuConstraint;
	}

	public AlternateSuccession discoverBranchedAlternateSuccessionConstraints(
			TaskChar pivotTaskCh,
			long pivotAppearances,
			TaskCharSet comboToAnalyze) {
		AlternateSuccession nuConstraint = null;
		
		LocalStatsWrapperForCharsets extPivotLocalStats = (LocalStatsWrapperForCharsets) (globalStats.statsTable.get(pivotTaskCh.identifier));
		SortedSet<CharactersSetCounter>
			neverAppearedCharSets = null,
			repetitionsBeforeAppearingAfterCharSets = null;
	
		int	negativeOccurrences = 0,
			denominator = 0;
		double support = 0;
		LocalStatsWrapper searchedStatsWrapper = null;
	
//		repetitionsBeforeAppearingAfterCharSets =
//				extPivotLocalStats.repetitionsBeforeCharactersAppearingAfter.selectCharSetCountersSharedAmong(comboToAnalyze.getListOfIdentifiers());
		neverAppearedCharSets =
				extPivotLocalStats.getNeverMoreAppearedAfterCharacterSets().selectCharSetCountersSharedAmong(comboToAnalyze.getListOfIdentifiers());
		
//		for (CharactersSetCounter repetitionsBeforeAppearingAfterCharSet : repetitionsBeforeAppearingAfterCharSets) {
//			negativeOccurrences += repetitionsBeforeAppearingAfterCharSet.getCounter();
//		}
		for (CharactersSetCounter neverAppearedCharSet : neverAppearedCharSets) {
			negativeOccurrences += neverAppearedCharSet.getCounter();
		}
		denominator += pivotAppearances;
		for (Character searched : comboToAnalyze.getListOfIdentifiers()) {
			searchedStatsWrapper = globalStats.statsTable.get(searched);
			negativeOccurrences += searchedStatsWrapper.localStatsTable.get(pivotTaskCh.identifier).howManyTimesItNeverAppearedBackwards();
			negativeOccurrences += searchedStatsWrapper.localStatsTable.get(pivotTaskCh.identifier).betweenBackwards;
			denominator += searchedStatsWrapper.getTotalAmountOfAppearances();
		}
		
		support = 1.0 - (double) negativeOccurrences / (double) denominator;
		nuConstraint = new AlternateSuccession(
				new TaskCharSet(pivotTaskCh),
				comboToAnalyze,
				support);
	
		return nuConstraint;
	}

	public ChainPrecedence discoverBranchedChainPrecedenceConstraints(
			TaskChar pivotTaskCh,
			TaskCharSet comboToAnalyze) {
		ChainPrecedence nuConstraint = null;
		
		int	positiveOccurrences = 0, denominator = 0;
		double support = 0;
		LocalStatsWrapper searchedStatsWrapper = null;
		Integer tmpPositiveOccurrencesAdder = null;
	
		positiveOccurrences = 0;
		
		for (Character searched : comboToAnalyze.getListOfIdentifiers()) {
			searchedStatsWrapper = globalStats.statsTable.get(searched);
			tmpPositiveOccurrencesAdder = searchedStatsWrapper.localStatsTable.get(pivotTaskCh.identifier).distances.get(-1);
			if (tmpPositiveOccurrencesAdder != null)
				positiveOccurrences += tmpPositiveOccurrencesAdder;
			denominator += searchedStatsWrapper.getTotalAmountOfAppearances();
		}
		support = (double) positiveOccurrences / (double) denominator;
		
		nuConstraint = new ChainPrecedence(
				new TaskCharSet(pivotTaskCh),
				comboToAnalyze,
				support);
		return nuConstraint;
	}

	public ChainResponse discoverBranchedChainResponseConstraints(
			TaskChar pivotTaskCh,
			LocalStatsWrapper pivotLocalStats,
			long pivotAppearances,
			TaskCharSet comboToAnalyze) {
		ChainResponse nuConstraint = null;
		if (pivotAppearances < 1)
			return nuConstraint;
	
		int	positiveOccurrences = 0;
		double support = 0;
		Integer tmpPositiveOccurrencesAdder = null;
	
		for (Character searched : comboToAnalyze.getListOfIdentifiers()) {
			tmpPositiveOccurrencesAdder = pivotLocalStats.localStatsTable.get(searched).distances.get(1);
			if (tmpPositiveOccurrencesAdder != null)
				positiveOccurrences += tmpPositiveOccurrencesAdder;
		}				
		support = (double) positiveOccurrences / (double) pivotAppearances;
		nuConstraint = new ChainResponse(
				new TaskCharSet(pivotTaskCh),
				comboToAnalyze,
				support);
	
		return nuConstraint;
	}

	public ChainSuccession discoverBranchedChainSuccessionConstraints(
				TaskChar pivotTaskCh,
				long pivotAppearances,
				TaskCharSet comboToAnalyze) {
			ChainSuccession nuConstraint = null;
			
			int	positiveOccurrences = 0,
				denominator = 0;
			double support = 0;
			Integer tmpPositiveOccurrencesAdder = null;
			LocalStatsWrapper
				pivotLocalStats = globalStats.statsTable.get(pivotTaskCh.identifier),
				searchedLocalStats = null;
	
			denominator = (int) pivotAppearances;
			
			for (Character searched : comboToAnalyze.getListOfIdentifiers()) {
				tmpPositiveOccurrencesAdder = pivotLocalStats.localStatsTable.get(searched).distances.get(1);
				if (tmpPositiveOccurrencesAdder != null)
					positiveOccurrences += tmpPositiveOccurrencesAdder;
	
				searchedLocalStats = globalStats.statsTable.get(searched);
				tmpPositiveOccurrencesAdder = searchedLocalStats.localStatsTable.get(pivotTaskCh.identifier).distances.get(-1);
				if (tmpPositiveOccurrencesAdder != null)
					positiveOccurrences += tmpPositiveOccurrencesAdder;
				denominator += searchedLocalStats.getTotalAmountOfAppearances();
			}
			support = (double) positiveOccurrences / (double) denominator;
			
			nuConstraint = new ChainSuccession(
					new TaskCharSet(pivotTaskCh),
					comboToAnalyze,
					support);
	/*
			nuConstraint = new NotChainSuccession(
					new TaskCharSet(pivotTaskCh),
					comboToAnalyze,
					Constraint.complementSupport(support));
	*/
			return nuConstraint;
		}

	public CoExistence discoverBranchedCoExistenceConstraints(
				TaskChar pivotTaskCh,
				long pivotAppearances,
				TaskCharSet comboToAnalyze) {
			CoExistence nuConstraint = null;
	
			LocalStatsWrapperForCharsets extPivotLocalStats = (LocalStatsWrapperForCharsets) (globalStats.statsTable.get(pivotTaskCh.identifier));
			SortedSet<CharactersSetCounter> neverAppearedCharSets = null;
	
			int	negativeOccurrences = 0,
				denominator = 0;
			double support = 0;
			LocalStatsWrapper searchedStatsWrapper = null;
	
			neverAppearedCharSets =
					extPivotLocalStats.getNeverAppearedCharacterSets().selectCharSetCountersSharedAmong(comboToAnalyze.getListOfIdentifiers());
			
			for (CharactersSetCounter neverAppearedCharSet : neverAppearedCharSets) {
				negativeOccurrences += neverAppearedCharSet.getCounter();
			}
			denominator += pivotAppearances;
			for (Character searched : comboToAnalyze.getListOfIdentifiers()) {
				searchedStatsWrapper = globalStats.statsTable.get(searched);
				negativeOccurrences += searchedStatsWrapper.localStatsTable.get(pivotTaskCh.identifier).howManyTimesItNeverAppearedAtAll();
				denominator += searchedStatsWrapper.getTotalAmountOfAppearances();
			}
			
			support = 1.0 - (double) negativeOccurrences / (double) denominator;
			nuConstraint = new CoExistence(
					new TaskCharSet(pivotTaskCh),
					comboToAnalyze,
					support);
	/*		
			nuConstraint = new NotCoExistence(
					new TaskCharSet(pivotTaskCh),
					comboToAnalyze,
					Constraint.complementSupport(support));
	*/
			return nuConstraint;
		}

	public Precedence discoverBranchedPrecedenceConstraints(
			TaskChar pivotTaskCh,
			TaskCharSet comboToAnalyze) {
		Precedence nuConstraint = null;
	
		int	negativeOccurrences = 0,
			denominator = 0;
		double support = 0;
		LocalStatsWrapper searchedStatsWrapper = null;
		negativeOccurrences = 0;
		denominator = 0;
		
		for (Character searched : comboToAnalyze.getListOfIdentifiers()) {
			searchedStatsWrapper = globalStats.statsTable.get(searched);
			negativeOccurrences += searchedStatsWrapper.localStatsTable.get(pivotTaskCh.identifier).howManyTimesItNeverAppearedBackwards();
			denominator += searchedStatsWrapper.getTotalAmountOfAppearances();
		}
		
		support = 1.0 - (double) negativeOccurrences / (double) denominator;
		nuConstraint = new Precedence(
				new TaskCharSet(pivotTaskCh),
				comboToAnalyze,
				support);
	
		return nuConstraint;
	}

	public RespondedExistence discoverBranchedRespondedExistenceConstraints(
			TaskChar pivotTaskCh,
			LocalStatsWrapper pivotLocalStats,
			long pivotAppearances,
			TaskCharSet comboToAnalyze) {
		RespondedExistence nuConstraint = null;
		if (pivotAppearances < 1)
			return nuConstraint;
	
		LocalStatsWrapperForCharsets extPivotLocalStats = (LocalStatsWrapperForCharsets) pivotLocalStats;
		
		SortedSet<CharactersSetCounter> neverAppearedCharSets = null;
		int negativeOccurrences = 0;
		double support = 0;
		
		neverAppearedCharSets =
				extPivotLocalStats.getNeverAppearedCharacterSets().selectCharSetCountersSharedAmong(comboToAnalyze.getListOfIdentifiers());
		if (neverAppearedCharSets.size() == 0) {
			nuConstraint = new RespondedExistence(
					new TaskCharSet(pivotTaskCh),
					comboToAnalyze,
					1.0);
		} else {
			for (CharactersSetCounter neverAppearedCharSet : neverAppearedCharSets) {
				negativeOccurrences += neverAppearedCharSet.getCounter();
			}
			support = 1.0 - (double)negativeOccurrences / (double)pivotAppearances;
			nuConstraint = new RespondedExistence(
					new TaskCharSet(pivotTaskCh),
					comboToAnalyze,
					support);
		}
	
		
		return nuConstraint;
	}

	public Response discoverBranchedResponseConstraints(
			TaskChar pivotTaskCh,
			LocalStatsWrapper pivotLocalStats,
			long pivotAppearances,
			TaskCharSet comboToAnalyze) {
		Response nuConstraint = null;
		if (pivotAppearances < 1)
			return nuConstraint;
	
		LocalStatsWrapperForCharsets extPivotLocalStats = (LocalStatsWrapperForCharsets) pivotLocalStats;
		
		SortedSet<CharactersSetCounter> neverAppearedCharSets = null;
		int negativeOccurrences = 0;
		double support = 0;
	
		neverAppearedCharSets =
				extPivotLocalStats.getNeverMoreAppearedAfterCharacterSets().selectCharSetCountersSharedAmong(comboToAnalyze.getListOfIdentifiers());
		
		if (neverAppearedCharSets.size() == 0) {
			nuConstraint = new Response(
					new TaskCharSet(pivotTaskCh),
					comboToAnalyze,
					1.0);
		} else {
			for (CharactersSetCounter neverAppearedAfterCharSet : neverAppearedCharSets) {
				negativeOccurrences += neverAppearedAfterCharSet.getCounter();
				support = 1.0 - (double)negativeOccurrences / (double)pivotAppearances;
				nuConstraint = new Response(
						new TaskCharSet(pivotTaskCh),
						comboToAnalyze,
						support);
			}
		}
		
		return nuConstraint;
	}

	public Succession discoverBranchedSuccessionConstraints(
				TaskChar pivotTaskCh,
				long pivotAppearances,
				TaskCharSet comboToAnalyze) {
			Succession nuConstraint = null;
	
			LocalStatsWrapperForCharsets extPivotLocalStats = (LocalStatsWrapperForCharsets) (globalStats.statsTable.get(pivotTaskCh.identifier));
			SortedSet<CharactersSetCounter> neverAppearedCharSets = null;
	
			int	negativeOccurrences = 0,
				denominator = 0;
			double support = 0;
			LocalStatsWrapper searchedStatsWrapper = null;
	
			neverAppearedCharSets =
					extPivotLocalStats.getNeverMoreAppearedAfterCharacterSets().selectCharSetCountersSharedAmong(comboToAnalyze.getListOfIdentifiers());
			
			for (CharactersSetCounter neverAppearedCharSet : neverAppearedCharSets) {
				negativeOccurrences += neverAppearedCharSet.getCounter();
			}
			denominator += pivotAppearances;
			for (Character searched : comboToAnalyze.getListOfIdentifiers()) {
				searchedStatsWrapper = globalStats.statsTable.get(searched);
				negativeOccurrences += searchedStatsWrapper.localStatsTable.get(pivotTaskCh.identifier).howManyTimesItNeverAppearedBackwards();
//				negativeOccurrences += searchedStatsWrapper.localStatsTable.get(pivot).betweenBackwards;
				denominator += searchedStatsWrapper.getTotalAmountOfAppearances();
			}
			
			support = 1.0 - (double) negativeOccurrences / (double) denominator;
			nuConstraint = new Succession(
					new TaskCharSet(pivotTaskCh),
					comboToAnalyze,
					support);
	/*
				nuConstraint = new NotSuccession(
						new TaskCharSet(pivotTaskCh),
						comboToAnalyze,
						Constraint.complementSupport(support));
	*/
			return nuConstraint;
		}
}