/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.miner;

import java.util.Set;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.AlternateResponse;
import minerful.concept.constraint.relation.AlternateSuccession;
import minerful.concept.constraint.relation.ChainPrecedence;
import minerful.concept.constraint.relation.ChainResponse;
import minerful.concept.constraint.relation.ChainSuccession;
import minerful.concept.constraint.relation.CoExistence;
import minerful.concept.constraint.relation.NotChainSuccession;
import minerful.concept.constraint.relation.NotCoExistence;
import minerful.concept.constraint.relation.NotSuccession;
import minerful.concept.constraint.relation.Precedence;
import minerful.concept.constraint.relation.RelationConstraint;
import minerful.concept.constraint.relation.RespondedExistence;
import minerful.concept.constraint.relation.Response;
import minerful.concept.constraint.relation.Succession;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.LocalStatsWrapper;
import minerful.miner.stats.StatsCell;

public class DeterministicRelationConstraintsMiner extends RelationConstraintsMiner {
    
    public DeterministicRelationConstraintsMiner(GlobalStatsTable globalStats, TaskCharArchive taskCharArchive) {
		super(globalStats, taskCharArchive);
	}

	@Override
    public TaskCharRelatedConstraintsBag discoverConstraints(TaskCharRelatedConstraintsBag constraintsBag) {
        /* Inizialization */
        if (constraintsBag == null)
            constraintsBag = new TaskCharRelatedConstraintsBag(taskCharArchive.getTaskChars());
        LocalStatsWrapper auxLocalStats = null;
        Set<Constraint> auxRelCons = super.makeTemporarySet(
        		MetaConstraintUtils.howManyPossibleConstraints(this.taskCharArchive.howManyTaskChars()));
        for (TaskChar tCh: this.taskCharArchive.getTaskChars()) {
            auxLocalStats = this.globalStats.statsTable.get(tCh.identifier);
            // Avoid the famous rule: EX FALSO QUOD LIBET! Meaning: if you have no occurrence of a character, each constraint is potentially valid on it. Thus, it is perfectly useless to indagate over it -- and believe me, if you remove this check, it actually happens you have every possible restrictive constraint as valid in the list!
            if (auxLocalStats.getTotalAmountOfAppearances() > 0) {
                auxRelCons.addAll(
                        this.discoverRelationConstraints(tCh));
            }
        }
        auxRelCons = this.refineRelationConstraints(auxRelCons);
        for (Constraint relCon: auxRelCons)
            constraintsBag.add(relCon.base, relCon);
        return constraintsBag;
    }

    // Very very rough: a little statistical analysis on the trend would be better
	@Override
    protected Set<Constraint> discoverRelationConstraints(TaskChar taskChUnderAnalysis) {
		Character character = taskChUnderAnalysis.identifier;
        LocalStatsWrapper localStats = globalStats.statsTable.get(character);
        // For each other character
        StatsCell auxStatsCell = null;
        boolean never = false, neverAfter = false, neverBefore = false,
                alwaysOneStepAfter = false, alwaysOneStepBefore = false,
                alwaysNeverAlternatingAfter = false, alwaysNeverAlternatingBefore = false,
                alwaysNever = false, alwaysNeverAfter = false, alwaysNeverOneStepAfter = false;
        Set<Constraint> relaCons = super.makeTemporarySet(
        		MetaConstraintUtils.howManyPossibleRelationConstraints(this.taskCharArchive.howManyTaskChars()));
        for (Character other: localStats.localStatsTable.keySet()) {
            never = false;
            neverAfter = false;
            neverBefore = false;
            alwaysOneStepAfter = false;
            alwaysOneStepBefore = false;
            alwaysNever = false;
            alwaysNeverAfter = false;
            alwaysNeverOneStepAfter = false;
            if (!other.equals(character)) {
                auxStatsCell = localStats.localStatsTable.get(other);
               // Did it ever happen to the analyzed character NOT to appear WHENEVER the base character occurred?
                never = (auxStatsCell.howManyTimesItNeverAppearedAtAll() > 0);
                // If not, probably it's a RespondedExistence
                if (!never) {
                    // If a RespondedExistence holds, is it a Response? To know this, you should check whether it NEVER happened to the analyzed character NOT to appear AFTER the base character occurred
                    neverAfter = (auxStatsCell.howManyTimesItNeverAppearedOnwards() > 0);
                    // If it is always true that AFTER the base character occurs, the analyzed one appears in the trace as well, then...
                    if (!neverAfter) {
                        // ... the AlternateResponse holds if and only if it NEVER happens that the base character appears in the middle of the subtrace between itself and the analyzed one AFTER
                        alwaysNeverAlternatingAfter = (
                                auxStatsCell.betweenOnwards == 0
                                );
                        if (alwaysNeverAlternatingAfter) {
                            // ... the ChainResponse holds if and only if the number of appearances of the analyzed character falling one step AFTER the base one is equal to or greater than the total amount of occurrences of the base character
                            alwaysOneStepAfter = (
                                    auxStatsCell.distances.get(1) != null
                                    && localStats.getTotalAmountOfAppearances() <=
                                    auxStatsCell.distances.get(1));
                        }
                    }
                    // If a RespondedExistence holds, is it a Precedence? To know this, you should check whether it NEVER happened to the analyzed character NOT to appear BEFORE the base character occurred
                    neverBefore = (auxStatsCell.howManyTimesItNeverAppearedBackwards() > 0);
                    // If it is always true that BEFORE the base character occurs, the analyzed one appears in the trace as well, then...
                    if (!neverBefore) {
                        // ... the AlternateResponse holds if and only if it NEVER happens that the OTHER character appears in the middle of the subtrace between itself and the analyzed one BEFORE
                        alwaysNeverAlternatingBefore = (
                                auxStatsCell.betweenOnwards == 0
                                );
                        if (alwaysNeverAlternatingBefore) {
                            // ... the ChainPrecedence holds if and only if the number of appearances of the analyzed character falling one step BEFORE the base one is equal to or greater than the total amount of occurrences of the base character
                            alwaysOneStepBefore = (
                                    auxStatsCell.distances.get(-1) != null
                                    && localStats.getTotalAmountOfAppearances() <=
                                    auxStatsCell.distances.get(-1));
                        }
                    }
                }
                // NotCoExistence(a, b)
                alwaysNever = (auxStatsCell.howManyTimesItNeverAppearedAtAll() == localStats.getTotalAmountOfAppearances());
                if (!alwaysNever) {
                    // NotSuccession
                    alwaysNeverAfter = auxStatsCell.howManyTimesItNeverAppearedOnwards() == localStats.getTotalAmountOfAppearances();
                    if (!alwaysNeverAfter) {
                        // NotChainSuccession
                        alwaysNeverOneStepAfter = auxStatsCell.distances.get(1) == null || auxStatsCell.distances.get(1) < 1;
                    }
                }
                
                if (!never) {
                    if (!neverAfter) {
                        if (alwaysNeverAlternatingAfter) {
                            if (alwaysOneStepAfter) {
                                relaCons.add(new ChainResponse(
                                        this.taskCharArchive.getTaskChar(character), this.taskCharArchive.getTaskChar(other)));
                            }
                            else {
                                relaCons.add(new AlternateResponse(
                                        this.taskCharArchive.getTaskChar(character), this.taskCharArchive.getTaskChar(other)));
                            }
                        }
                        else
                            relaCons.add(new Response(
                                    this.taskCharArchive.getTaskChar(character), this.taskCharArchive.getTaskChar(other)));
                    }
                    if (!neverBefore) {
                        if (alwaysNeverAlternatingBefore) {
                            if (alwaysOneStepBefore) {
                                relaCons.add(new ChainPrecedence(
                                        this.taskCharArchive.getTaskChar(other), this.taskCharArchive.getTaskChar(character)));
                            }
                            else {
                                relaCons.add(new AlternatePrecedence(
                                        this.taskCharArchive.getTaskChar(other), this.taskCharArchive.getTaskChar(character)));
                            }
                        }
                        else
                            relaCons.add(new Precedence(
                                    this.taskCharArchive.getTaskChar(other), this.taskCharArchive.getTaskChar(character)));
                    }
                    if (neverAfter && neverBefore) {
                        relaCons.add(new RespondedExistence(
                                    this.taskCharArchive.getTaskChar(character), this.taskCharArchive.getTaskChar(other)));
                    }
                    
                }
                if (alwaysNever) {
                    relaCons.add(new NotCoExistence(
                                this.taskCharArchive.getTaskChar(character), this.taskCharArchive.getTaskChar(other)
                            )
                    );
                } else {
                    if (alwaysNeverAfter) {
                        relaCons.add(new NotSuccession(
                                this.taskCharArchive.getTaskChar(character), this.taskCharArchive.getTaskChar(other)
                            )
                        );
                    } else {
                        if (alwaysNeverOneStepAfter) {
                            relaCons.add(new NotChainSuccession(
                                    this.taskCharArchive.getTaskChar(character), this.taskCharArchive.getTaskChar(other)
                                )
                            );
                        }
                    }
                }
            }
        }
        return relaCons;
    }

	@Override
    protected Set<Constraint> refineRelationConstraints(Set<Constraint> setOfConstraints) {
        Set<Constraint> auxSet = super.makeTemporarySet(
        		MetaConstraintUtils.howManyPossibleConstraints(this.taskCharArchive.howManyTaskChars()));

        RelationConstraint auxConstraint = null, testConstraint = null;
        RelationConstraint[] refinedConstraints = null;
        for (Constraint c: auxSet) {
            auxConstraint = (RelationConstraint)c;
            // ChainSuccession(a, b) == ChainResponse(a, b) && ChainPrecedence(a, b)
            if (auxConstraint instanceof ChainPrecedence) {
                testConstraint = new ChainResponse(auxConstraint.base, auxConstraint.implied);
                if (setOfConstraints.contains(testConstraint)) {
                    refinedConstraints = new RelationConstraint[] {
                            new ChainSuccession(
                                    auxConstraint.base, auxConstraint.implied)
                    };
                }
            } else if (auxConstraint instanceof ChainResponse) {
                testConstraint = new ChainPrecedence(auxConstraint.base, auxConstraint.implied);
                if (setOfConstraints.contains(testConstraint)) {
                    refinedConstraints = new RelationConstraint[] {
                            new ChainSuccession(
                                    auxConstraint.base, auxConstraint.implied)
                    };
                }
            }
            // AlternateSuccession(a, b) == AlternateResponse(a, b) && AlternatePrecedence(a, b)
            else if (auxConstraint instanceof AlternatePrecedence) {
                testConstraint = new AlternateResponse(auxConstraint.base, auxConstraint.implied);
                if (setOfConstraints.contains(testConstraint)) {
                    refinedConstraints = new RelationConstraint[] {
                            new AlternateSuccession(
                                    auxConstraint.base, auxConstraint.implied)
                    };
                }
            } else if (auxConstraint instanceof AlternateResponse) {
                testConstraint = new AlternatePrecedence(auxConstraint.base, auxConstraint.implied);
                if (setOfConstraints.contains(testConstraint)) {
                    refinedConstraints = new RelationConstraint[] {
                            new AlternateSuccession(
                                    auxConstraint.base, auxConstraint.implied)
                    };
                }
            }
            // Succession(a, b) == Response(a, b) && Precedence(a, b)
            else if (auxConstraint instanceof Precedence) {
                testConstraint = new Response(auxConstraint.base, auxConstraint.implied);
                if (setOfConstraints.contains(testConstraint)) {
                    refinedConstraints = new RelationConstraint[] {
                            new Succession(
                                    auxConstraint.base, auxConstraint.implied)
                    };
                }
            } else if (auxConstraint instanceof Response) {
                testConstraint = new Precedence(auxConstraint.base, auxConstraint.implied);
                if (setOfConstraints.contains(testConstraint)) {
                    refinedConstraints = new RelationConstraint[] {
                            new Succession(
                                    auxConstraint.base, auxConstraint.implied)
                    };
                }
            }
            // CoExistence(a, b) == RespondedExistence(a, b) && RespondedExistence(b, a)
            else if (auxConstraint instanceof RespondedExistence) {
                testConstraint = new RespondedExistence(auxConstraint.implied, auxConstraint.base);
                if (setOfConstraints.contains(testConstraint)) {
                    refinedConstraints = new RelationConstraint[] {
                            new CoExistence(
                                    auxConstraint.base, auxConstraint.implied),
                            new CoExistence(
                                    auxConstraint.implied, auxConstraint.base),
                    };
                }
            }
            if (refinedConstraints != null) {
                    setOfConstraints.remove(auxConstraint);
                    setOfConstraints.remove(testConstraint);
                    for (Constraint refinedConstraint: refinedConstraints)
                        setOfConstraints.add(refinedConstraint);
            }
            testConstraint = null;
            refinedConstraints = null;
        }
        return setOfConstraints;
    }
}