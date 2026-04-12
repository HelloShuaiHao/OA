You are Codex working inside the ruoyi-vue-pro repository.

Task type: bugfix
Scope: {{SCOPE}}
Target modules or paths: {{TARGETS}}
Branch: {{BRANCH}}
Worktree: {{WORKTREE}}

Repository facts:
- Backend: Java 8, Spring Boot 2.7, multi-module Maven.
- Frontend: Vue3 app at yudao-ui/yudao-ui-admin-vue3 using pnpm.
- Prefer the smallest safe fix over broad refactors.
- Do not change unrelated modules.

Execution rules:
1. Reproduce or reason about the bug from the described scope.
2. Fix root cause, not only the symptom, when feasible within scope.
3. Run the narrowest relevant verification commands.
4. Commit your changes with a clear message.
5. If gh is configured, open or update a PR.

Definition of done:
- The bug is fixed within the scoped area.
- Relevant checks pass.
- A commit exists on the task branch.
- If a PR is created, include cause, fix, and validation notes.

When you finish, print a short final summary with:
- files changed
- checks run
- commit sha
- pr number or 'none'
