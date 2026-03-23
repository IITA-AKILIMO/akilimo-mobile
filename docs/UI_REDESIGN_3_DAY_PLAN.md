# AKILIMO Cassava Recommendation App — 3-Day UI Redesign Plan

## Purpose

This document outlines a focused **3-day UI redesign plan** for the AKILIMO cassava recommendation mobile app. The redesign is intentionally limited to **visual hierarchy, component consistency, layout clarity, and usability improvements** while preserving all current behavior.

## Non-Negotiable Constraints

- **Do not change business logic, APIs, repositories, or ViewModels.**
- **Do not remove or alter features.**
- **Preserve all user flows, validation rules, and navigation paths.**
- **Maintain compatibility with minSdkVersion 21.**
- **Keep the UI lightweight for low-end Android devices.**

## Current UI Foundation Already in the Codebase

The app already has a strong Compose base that this redesign should build on rather than replace:

- `AkilimoTheme` already wraps Material 3 and centralizes color, typography, and shapes.
- The existing theme uses a nature-forward palette with strong green tones and high-contrast light/dark schemes.
- Reusable Compose building blocks already exist for selection cards, text fields, dropdowns, app bars, and bottom action bars.
- Many core workflows already use `LazyColumn`, `Scaffold`, `SelectionCard`, and persistent bottom actions, which makes the redesign feasible without touching business logic.

## Redesign Objectives

### 1. Make the app scannable in 2–3 seconds
Users should immediately understand:
- where they are,
- what they need to do next,
- what has already been selected,
- and how to move forward.

### 2. Improve confidence in selections
For farmers and field agents, every selected item must be visually unmistakable through:
- color,
- border,
- icon/check indicator,
- and clearer supporting text.

### 3. Reduce visual clutter without reducing information
The app should feel calmer and more professional by:
- grouping related content into cards/sections,
- shortening on-screen explanatory text,
- surfacing the most important action clearly,
- and using consistent spacing.

### 4. Preserve task completion flows
All redesign work must continue to support the existing recommendation workflow:
- recommendations list,
- use-case task completion list,
- input forms and selectors,
- final recommendation result screens,
- settings and onboarding continuity.

## Proposed Visual System

### Color direction
Use the current AKILIMO palette as the base, but apply it more systematically:
- **Primary green** for main actions and selected states.
- **Primary container / tinted surface** for selected cards and highlighted sections.
- **Surface / surfaceVariant** for neutral section grouping.
- **Error / warning / info semantic colors** only where status messaging is needed.

### Typography direction
Use the existing Material 3 typography scale, but tighten usage:
- `headlineSmall` / `titleLarge` for screen titles,
- `titleMedium` for section headers,
- `bodyLarge` for important selectable labels,
- `bodyMedium` for supporting descriptions,
- `labelLarge` for buttons and status labels.

### Spacing system
Adopt a strict 8dp rhythm:
- 8dp between closely related elements,
- 16dp for section padding,
- 24dp between major sections,
- 48dp minimum touch target,
- avoid cramped card grids unless the visual scan remains strong.

### Shape and elevation direction
Use the existing rounded Material 3 shapes with restraint:
- compact controls: small,
- standard cards: medium,
- sheets/dialogs: large,
- prefer contrast, border, and spacing over heavy shadows.

## Reusable UI Components to Standardize

The redesign should standardize a compact set of shared composables so all screens feel part of one system.

### 1. Screen scaffold pattern
Create a consistent screen structure for all input and recommendation screens:
- top app bar with concise title,
- optional short guidance text under the title,
- main `LazyColumn` content area,
- persistent bottom primary action.

### 2. Section card
A reusable section wrapper for grouped inputs:
- section title,
- optional helper text,
- internal spacing,
- neutral background with subtle border or tonal fill.

Use this for:
- forms,
- grouped selectors,
- recommendation result sections,
- summary blocks.

### 3. Primary and secondary action buttons
Standardize action hierarchy:
- one clear primary CTA per screen,
- secondary actions only when necessary,
- disabled state should remain obvious,
- loading and retry actions should match the same visual language.

### 4. Selected option card / tile
Enhance the current selection card pattern so selected states are unmistakable:
- selected background tint,
- visible outline stroke,
- check icon or selected badge,
- improved spacing and text hierarchy,
- optional support for grid or list layouts depending on content density.

### 5. Form field block
For text/date/dropdown screens:
- label above field,
- helper text when needed,
- larger spacing between fields,
- clearer error/help messaging,
- better section grouping instead of long undifferentiated forms.

### 6. Progress / next-step guidance block
A lightweight informational component near the top of flow screens that answers:
- what this screen is for,
- why the input matters,
- what happens after saving.

This is especially useful for agricultural guidance screens where confidence matters.

## Screen-Level Redesign Priorities

### A. Recommendations entry screen
Current role:
- Shows the list of available recommendation categories.

Redesign intent:
- Introduce a stronger hero/title area.
- Add a concise subtext such as “Choose the advice you want to prepare.”
- Increase card clarity so each recommendation type feels like a distinct, tappable pathway.
- Make the next-step affordance more explicit with stronger action cues and spacing.

### B. Use-case task checklist screen
Current role:
- Shows the tasks required before getting a recommendation.

Redesign intent:
- Convert the checklist into clearer grouped task cards with stronger completion states.
- Surface progress clearly at the top, for example as completed tasks out of total tasks.
- Add short helper copy clarifying that users can skip optional-looking questions only if the workflow already permits it.
- Keep the bottom “Get recommendations” action visually dominant and clearly tied to task completion.

### C. Selection-heavy screens
Examples:
- fertilizer selection,
- cassava yield selection,
- market outlet selection,
- investment amount selection.

Redesign intent:
- Make selection tiles larger and easier to scan outdoors.
- Improve selected-state visibility with border + color + check indicator.
- Reduce ambiguity in list/grid mode by aligning text hierarchy and spacing.
- Use section headers such as “Select one option” or “Select all that apply” where relevant.
- Preserve layout toggle functionality where it already exists.

### D. Form-heavy screens
Examples:
- dates,
- costs,
- settings/profile fields,
- location-related forms.

Redesign intent:
- Break large forms into sections.
- Place labels above fields instead of relying on hint-only patterns.
- Increase vertical spacing between fields.
- Add contextual helper text only where it prevents user confusion.
- Ensure save/next actions remain visible and unambiguous.

### E. Final recommendation result screen
Current role:
- Fetches and shows recommendation output.

Redesign intent:
- Promote the recommendation title into a strong summary header.
- Present the core recommendation in a highlighted summary card.
- Separate supporting details into secondary cards/sections.
- Improve loading and error states so they feel intentional and trustworthy.
- Keep the feedback action accessible but less visually disconnected from the result content.

## 3-Day Execution Plan

---

## Day 1 — Foundation, UI Audit, and Design System Alignment

### Goal
Create the shared visual language and identify which high-impact screens can be redesigned fastest without touching logic.

### Focus areas
1. **Audit existing Compose screens and components**
   - Review recommendation flows, onboarding steps, input screens, settings, and result screens.
   - Identify duplicated spacing, inconsistent card usage, inconsistent button hierarchy, and weak selected states.

2. **Define a formal UI system for the redesign**
   - Confirm color role usage from the existing theme.
   - Confirm typography usage rules by screen type.
   - Define spacing tokens based on the 8dp system.
   - Define section/card standards and selected-state standards.

3. **Plan reusable component upgrades**
   - document the updated spec for selection cards,
   - section cards,
   - action bars,
   - form blocks,
   - status/progress indicators.

4. **Prioritize implementation order**
   - Identify the 3–5 most visible screens for immediate redesign:
     - recommendations list,
     - use-case task checklist,
     - fertilizer selection,
     - cassava yield selection,
     - final recommendation result.

### Day 1 deliverables
- Finalized visual system rules.
- Shared component inventory with redesign notes.
- Screen prioritization list.
- Annotated implementation checklist showing which screens reuse which components.

### Day 1 success criteria
- Team alignment on one visual direction.
- No changes to logic are required for the redesign plan.
- All major redesign work is mapped to reusable Compose components instead of one-off screen tweaks.

---

## Day 2 — High-Impact Screen Redesign and Component Application

### Goal
Redesign the most visible and workflow-critical screens using the shared component system.

### Focus areas
1. **Recommendations list screen**
   - Improve information hierarchy.
   - Make each advice option more visually structured and premium.
   - Clarify tap affordance and forward navigation.

2. **Use-case task checklist screen**
   - Introduce a progress summary.
   - Redesign task rows/cards for better completion visibility.
   - Make the CTA feel more connected to task readiness.

3. **Selection-heavy screens**
   - Upgrade fertilizer, yield, and investment selection patterns.
   - Apply stronger selected-state styling.
   - Improve list/grid readability and touch comfort.

4. **Form section pattern**
   - Apply section grouping and clearer labels to form-based screens.
   - Reduce long uninterrupted content blocks.

### Day 2 deliverables
- Updated reusable selection component spec and implementation approach.
- Updated screen structure for recommendation entry and task checklist screens.
- Updated pattern for selection and form screens.
- UI review notes confirming that all interactions remain unchanged.

### Day 2 success criteria
- The most-used workflows look visibly more modern and easier to scan.
- Selected states are obvious at a glance.
- Each redesigned screen has one clear next action.
- No screen introduces deeper nesting or higher cognitive load.

---

## Day 3 — Result Experience, Polish, Accessibility, and QA

### Goal
Polish the recommendation result experience and verify consistency, usability, and implementation safety.

### Focus areas
1. **Recommendation result screen polish**
   - Highlight the key recommendation prominently.
   - Separate summary, explanation, and actions into structured cards.
   - Improve loading, empty, and error-state presentation.

2. **Accessibility and outdoor usability pass**
   - Validate contrast on selected and unselected states.
   - Verify touch targets are at least 48dp.
   - Check text hierarchy for readability in bright light conditions.
   - Simplify labels that feel too technical.

3. **Consistency pass across the flow**
   - Confirm spacing, button hierarchy, headers, and cards feel uniform.
   - Verify that save/get recommendation actions appear in predictable locations.
   - Ensure helper text is short, purposeful, and not repetitive.

4. **Implementation safety and QA**
   - Confirm no ViewModel, navigation, or API behavior changed.
   - Run regression testing for all existing recommendation workflows.
   - Verify responsiveness across smaller and larger phones.
   - Capture before/after review notes for handoff.

### Day 3 deliverables
- Final result screen redesign spec.
- Accessibility and usability checklist.
- QA/regression checklist.
- Final handoff summary describing reusable components and screen updates.

### Day 3 success criteria
- The result screen clearly communicates the recommendation in seconds.
- The experience feels consistent end-to-end.
- The redesign remains fully compatible with the current architecture and app behavior.

## Recommended Implementation Sequence

If design and engineering happen together, implement in this order:

1. Theme usage cleanup and spacing standards.
2. Shared section and selection card improvements.
3. Recommendation list screen.
4. Use-case checklist screen.
5. High-traffic selection/form screens.
6. Final recommendation result screen.
7. Accessibility and polish pass.

## QA Checklist for the Redesign

### Functional safety
- Recommendation categories still navigate to the same routes.
- Task completion still writes completion state exactly as before.
- Save and finish buttons preserve existing enable/disable behavior.
- Recommendation fetching and feedback submission remain unchanged.

### UX quality
- Primary CTA is obvious on every screen.
- Selection states are visible without relying on text alone.
- Screens remain readable in bright outdoor use.
- Content can be scanned quickly without long dense paragraphs.
- Form fields and selectors have comfortable vertical spacing.

### Technical safety
- No ViewModel or repository logic is modified.
- No API contract changes are introduced.
- No minSdk-incompatible UI APIs are added.
- No heavy animation or expensive rendering is introduced.

## Brief Improvement Summary

This redesign improves the AKILIMO cassava recommendation experience by:

- making the interface easier to scan and trust,
- strengthening selected and completed states,
- clarifying the next step on every screen,
- reducing clutter through section-based layouts,
- standardizing reusable Compose patterns,
- and preserving all existing workflows and logic.

## Final Recommendation

Treat this as a **3-day UI modernization sprint**, not a product rewrite. The fastest path to a high-quality outcome is to:
- refine the theme usage already present,
- strengthen reusable components,
- redesign the highest-traffic recommendation screens first,
- and finish with result-screen polish plus accessibility validation.

That approach delivers a more modern, calm, professional experience for farmers and field agents while keeping the app stable and implementation risk low.
