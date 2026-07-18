# ADR-0006: BOM alignment

- **Status**: Pending (Open)
- **Date**: 2026-07-17
- **Suggested by**: OpenStrata Architecture Group
- **Repository**: ai-admin-service
- **Source**: `docs/DESIGN.md` §16 Open Issue
- **Association**: (within this repository)

##Context

CRD/API changes for Capsule 1.9 → 2.x and Keycloak 25 → 26 need to be tracked in `bom.yaml` (§16.1). --- > Change Record > | Version | Date | Description | > | --- | --- | --- | > | v1.0-Draft | 2026-07-17 | Initial detailed design, covering skeletal skeleton, 16 sections complete | > Traceability Matrix (this document section ↔ Architectural Design Document § Number) > | Section | Architecture Document § | > | --- | --- | > | 1 Domain Context | §14.1 / §4.7 / §8.1 | > | 2 Responsibility list | §14 / §4.7 | > | 3 Domain model | §15.5.2 / §14.2~14.5 | > | 4 Application layer use cases | §15.5.2 ② | > | 5 Domain service rules | §14.2 / §14.4 / §14.5 / §12.4 | > | 6 SPI ports and adapters | §10.3 / §10.4 / §15.5.4 | > | 7 External API contract | §14 / §16.4 | > | 8 Data model | §14.6 / §8.2 / §16 base | > | 9 Business process timing | §14.5 / §13.3 / §15.5.2.2 | > | 10 Configuration and Profiles | §12.1 / §12.2 / §12.4 | > | 11 Integration Points | §4.7.3 / §15.2 / bom.yaml | > | 12 Security and Multitenancy | §8 / §14.3 / §14.6 / §4.7.4 | > | 13 Observability | §4.8 | > | 14 Deployment and Resilience | §9.2 | > | 15 Testing Strategy | §15.5.5 | > | 16 Open Issues | — |

## Decision Options (Options Considered)

1. **Maintain status quo / conservative default**: Maintain current behavior, controlled by configuration switches or explicit parameters, and do not introduce destructive changes.
2. **Unified implementation after cross-repository alignment**: Agree on a clear contract with the relevant service (`corresponding governance service`) before implementation.
3. **Phased introduction**: Leave a placeholder/default switch in the current stage, and solidify it in subsequent stages after the dependent capabilities are ready (see Related Architecture §).

## Recommended decision (Decision)

This ADR solidifies "BOM alignment" into an architectural decision record and incorporates it into `docs/adr/` for continuous tracking. This issue stems from the `docs/DESIGN.md` §16 open issue and is still open.

**Conservative Default Principle**: Before the final decision is made, the "minimum available + explicit configuration switch" shall prevail, maintain the current behavior, and not destroy the existing contract and cross-repository SPI interface; this ADR status will be written back after review by the relevant team.



## To be aligned / Follow-ups (Follow-ups)

- Associated architecture documents §10.3 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §10.4 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §12.1 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §12.2 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §12.4 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §13.3 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §14 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §14.1 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §14.2 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §14.3 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §14.4 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §14.5 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §14.6 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §15.2 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §15.5.2 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §15.5.2.2 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §15.5.4 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §15.5.5 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §16 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §16.1 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §16.4 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §4.7 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §4.7.3 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §4.7.4 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §4.8 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §8 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §8.1 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §8.2 (as a basis for decision-making and a source of consistency verification).
- Associated architecture documents §9.2 (as a basis for decision-making and a source of consistency verification).
- Solidify the decision before the review at the corresponding stage, and write the final conclusion back into this ADR (the status is changed from "Pending" to "Adopted").

## Traceback

- Upstream design: `docs/DESIGN.md` §16 Open issue
- Relevance index: see `docs/adr/README.md`
