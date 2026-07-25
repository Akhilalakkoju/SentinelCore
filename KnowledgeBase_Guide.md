# Internal Knowledge Base (SOC Operations Hub)

This document provides a comprehensive overview of the **Internal Knowledge Base** implemented in SentinelCore. Use this guide to explain the architecture, components, and operational value of the documentation hub to your supervisor or stakeholders.

---

## 1. What is the Internal Knowledge Base?

> **"The Internal Knowledge Base is a centralized wiki that stores operational documentation such as runbooks, post-incident reviews, and detection-rule documentation. It helps security analysts quickly find standardized procedures, learn from past incidents, and understand how security threats are detected."**

In a Security Operations Center (SOC), memory is not scalable. The Knowledge Base serves as the **SOC's single source of truth**, standardizing response procedures, tracking incident post-mortems, and detailing SIEM rules to ensure consistency and efficiency across all analyst tiers.

---

## 2. Core Components

### 📘 A. Runbooks ("How do we respond?")
* **Definition:** Step-by-step operational procedures for performing investigation, containment, and mitigation tasks.
* **Examples:**
  * *Malware Containment Runbook* (e.g., isolating host, blocking hashes)
  * *Phishing Investigation Runbook* (e.g., extracting headers, deleting mail)
  * *Password Reset and Account Lockout Procedures*
* **Purpose:** 
  * Standardizes actions to eliminate human error.
  * Accelerates onboarding for junior analysts by providing pre-vetted procedures.

### 📝 B. Post-Incident Reviews / PIRs ("What happened and what did we learn?")
* **Definition:** Auditing documents created after an incident is resolved to analyze the root cause, business impact, response timeline, and preventive actions.
* **Structure:**
  * **What happened?** (Incident overview, source, and triage details)
  * **Root cause** (The vulnerability, exploit, or credential compromise details)
  * **Impact** (Affected assets, compromised data, and business downtime)
  * **Timeline** (Milestones from detection and triage to resolution)
  * **Lessons learned** (Response bottlenecks and successes)
  * **Preventive measures** (Hardening actions to prevent recurrence)
* **Purpose:**
  * Enables continuous improvement.
  * Drives cybersecurity hardening policies based on empirical post-incident analysis.

### ⚙️ C. Detection Rules ("How do we detect threats?")
* **Definition:** Documentation defining the criteria and logic the SIEM/detection engine uses to flag suspicious activity and trigger alerts.
* **Examples:**
  * *Multiple Failed Logins Rule:* Trigger high-severity alert if >5 authentication failures occur within 10 minutes from a single IP.
  * *Suspicious PowerShell Rule:* Flag encoded PowerShell commands executed from temp directories.
* **Purpose:**
  * Documents the security monitoring scope.
  * Bridges the gap between security analysts and detection engineers.

---

## 3. Difference and Relationship Between Components

| Component | Operational Purpose | SOC Analogy | Example |
| :--- | :--- | :--- | :--- |
| **Detection Rule** | **Identifies the threat** automatically in real-time. | *The Security Alarm* | Alert on >5 failed logins in 10 minutes. |
| **Runbook** | **Defines response steps** to contain and resolve the threat. | *The Fire Drill Plan* | Lock out user accounts, isolate host machines. |
| **PIR (Post-Incident Review)** | **Documents outcomes** and plans long-term security prevention. | *The Fire Marshal's Report* | Identify phishing root cause and patch credentials. |

---

## 4. Key Interactive Workflows Implemented

1. **Awaiting Review Panel:**
   * When an incident is marked **Resolved**, it immediately registers under the **PIRs > Awaiting Review** category in the Knowledge Base. 
   * This ensures no incident is closed without proper post-mortem documentation.
2. **One-Click PIR Template Generation:**
   * Clicking **Draft PIR** automatically constructs a new markdown article with pre-populated incident fields (Title, Severity, Source, Logged Date) and inserts the standard auditing sections.
3. **Linked Incident Context Card:**
   * Viewing any Post-Incident Review displays a live context card summarizing the linked incident's real-time severity, status, and log parameters at the top of the document.
4. **Full Version Tracking (Revisions history):**
   * Every edit logs a version milestone. Analysts can review version histories, compare text, or restore a prior document state instantly.
