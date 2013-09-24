/*
 * Sonar :: Issues Report :: Plugin
 * Copyright (C) 2013 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.issuesreport.report;

import com.google.common.collect.Maps;
import org.sonar.api.issue.Issue;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ReportSummary {

  private final class RuleReportComparator implements Comparator<RuleReport> {
    @Override
    public int compare(RuleReport o1, RuleReport o2) {
      if (o1.getTotal().getNewIssuesCount() == 0 && o2.getTotal().getNewIssuesCount() == 0) {
        // Compare with severity then name
        return o1.getReportRuleKey().compareTo(o2.getReportRuleKey());
      } else if (o1.getTotal().getNewIssuesCount() > 0 && o2.getTotal().getNewIssuesCount() > 0) {
        // Compare with severity then number of new issues
        if (o1.getSeverity().equals(o2.getSeverity())) {
          return o1.getTotal().getNewIssuesCount() - o2.getTotal().getNewIssuesCount();
        } else {
          return o1.getReportRuleKey().compareTo(o2.getReportRuleKey());
        }
      } else {
        // Compare with number of new issues
        return o1.getTotal().getNewIssuesCount() - o2.getTotal().getNewIssuesCount();
      }
    }
  }

  private final IssueVariation total = new IssueVariation();

  private final Map<ReportRuleKey, RuleReport> ruleReportByRuleKey = Maps.newHashMap();
  private final Map<String, IssueVariation> totalByRuleKey = Maps.newHashMap();
  private final Map<String, IssueVariation> totalBySeverity = Maps.newHashMap();

  public ReportSummary() {
  }

  public IssueVariation getTotal() {
    return total;
  }

  public void addIssue(Issue issue, Rule rule, RulePriority severity) {
    ReportRuleKey reportRuleKey = new ReportRuleKey(rule, severity);
    initMaps(reportRuleKey);
    ruleReportByRuleKey.get(reportRuleKey).getTotal().incrementCountInCurrentAnalysis();
    total.incrementCountInCurrentAnalysis();
    totalByRuleKey.get(rule.ruleKey().toString()).incrementCountInCurrentAnalysis();
    totalBySeverity.get(severity.toString()).incrementCountInCurrentAnalysis();
    if (issue.isNew()) {
      initMaps(reportRuleKey);
      total.incrementNewIssuesCount();
      ruleReportByRuleKey.get(reportRuleKey).getTotal().incrementNewIssuesCount();
      totalByRuleKey.get(rule.ruleKey().toString()).incrementNewIssuesCount();
      totalBySeverity.get(severity.toString()).incrementNewIssuesCount();
    }
  }

  public Map<String, IssueVariation> getTotalBySeverity() {
    return totalBySeverity;
  }

  public Map<String, IssueVariation> getTotalByRuleKey() {
    return totalByRuleKey;
  }

  public void addResolvedIssue(Issue issue, Rule rule, RulePriority severity) {
    ReportRuleKey reportRuleKey = new ReportRuleKey(rule, severity);
    initMaps(reportRuleKey);
    total.incrementResolvedIssuesCount();
    ruleReportByRuleKey.get(reportRuleKey).getTotal().incrementResolvedIssuesCount();
    totalByRuleKey.get(rule.ruleKey().toString()).incrementResolvedIssuesCount();
    totalBySeverity.get(severity.toString()).incrementResolvedIssuesCount();
  }

  private void initMaps(ReportRuleKey reportRuleKey) {
    if (!ruleReportByRuleKey.containsKey(reportRuleKey)) {
      ruleReportByRuleKey.put(reportRuleKey, new RuleReport(reportRuleKey));
    }
    if (!totalByRuleKey.containsKey(reportRuleKey.getRule().ruleKey().toString())) {
      totalByRuleKey.put(reportRuleKey.getRule().ruleKey().toString(), new IssueVariation());
    }
    if (!totalBySeverity.containsKey(reportRuleKey.getSeverity().toString())) {
      totalBySeverity.put(reportRuleKey.getSeverity().toString(), new IssueVariation());
    }
  }

  public List<RuleReport> getRuleReports() {
    List<RuleReport> result = new ArrayList<RuleReport>(ruleReportByRuleKey.values());
    Collections.sort(result, new RuleReportComparator());
    return result;
  }
}