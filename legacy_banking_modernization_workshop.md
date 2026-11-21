# Legacy Banking Application Modernization Workshop

## 1. Objective

Modernize the legacy banking app into a web application:

- **Frontend:** React + TypeScript
- **Backend:** Java 21 + Spring Boot
- **Database:** Existing SQLite database (default)
- **Build and run:** Maven, Node.js/npm and optionally Docker Compose

The workshop has three stages:

1. **Plan:** Analyze the legacy app and create a GitHub Epic with linked Stories.
2. **Implement:** Build the approved Stories.
3. **Validate and run:** Run tests, validate the app and run it locally.

> **Database migration is optional.** The default keeps the SQLite database. For an extra challenge (for example, SQLite to PostgreSQL), tell Bob during planning.

---

## 2. Prerequisites

Install the tools below, then open a new terminal. The commands in this guide work on macOS, Linux and Windows.

| Tool | Version | Verify |
|------|---------|--------|
| JDK | 21 (or the version your facilitator gives) | `java -version` |
| Maven | 3.6+ | `mvn -version` |
| Git | Recent | `git --version` |
| Node.js | LTS (includes npm) | `node --version` and `npm --version` |
| Docker (optional) | Recent | `docker compose version` |

Docker is needed only if the approved app uses Docker Compose.

You also need:

- A GitHub account that can create repositories, Issues, branches and Pull Requests.
- Git able to authenticate to GitHub from your terminal (HTTPS token or SSH key).
- Bob connected to GitHub with access to your repository. Complete any organization approval before the workshop.

---

## 3. Set up your repository

You clone the facilitator's legacy repository, but push all of your work to your own repository.

**Step 1: Create an empty repository on GitHub**

Create it under your account, for example `modern-banking-app`. Do not add a README, license or `.gitignore`. Copy its URL.

**Step 2: Clone the legacy repository and point `origin` to your repository**

```bash
git clone <PUBLIC_LEGACY_REPOSITORY_URL>
cd <LEGACY_REPOSITORY_FOLDER>
git remote remove origin
git remote add origin <YOUR_REPOSITORY_URL>
git remote -v
git ls-remote origin
```

`git remote -v` should show your repository. `git ls-remote origin` should return without an authentication error. It prints nothing for an empty repository, which is expected.

**Step 3: Push `main` to your repository**

```bash
git push -u origin main
```

This creates `main` in your repository, which Stage 3 uses as the Pull Request base.

**Step 4: Build and start the legacy app once**

```bash
mvn clean package
java -jar target/legacy-banking-app.jar
```

The first start creates `legacybank.db` in this folder. Log in with `admin` / `admin123` to confirm it works, then close the window.

**Step 5: Open the folder in your IDE**

Open the cloned folder, launch Bob in it and confirm that Bob uses your repository (`origin`) for all GitHub operations.

---

## 4. Run the prompts in sequence

Run the three prompts **one at a time**. Pass each checkpoint before starting the next stage.

### Stage 1: Plan Mode

Switch Bob to **Plan Mode** and submit:

> Analyze the existing legacy banking application and help me plan its
> modernization.
>
> -   Understand the existing functionality, architecture, business
>     rules and database schema.
> -   Keep the existing legacy source code and SQLite database strictly
>     read-only.
> -   Propose a modern architecture using React, Spring Boot and a
>     relational database.
> -   Use the existing SQLite database by default to keep the
>     modernization focused and within the workshop timeline.
> -   Include a simple UI enhancement to make the dashboard look clean,
>     modern and presentable for the demo.
> -   Identify major development tasks and create a GitHub Epic with
>     linked Stories, including acceptance criteria.
> -   Suggest an implementation sequence.
>
> **Important:** - Use my GitHub repository configured as `origin` for
> all GitHub Issues and subsequent modernization work. Do not create or
> push anything to the facilitator's public repository. - Whenever
> multiple reasonable technical approaches or design choices exist,
> briefly explain the options, recommend one and ask me to choose before
> finalizing the plan. - Database migration (e.g., SQLite to PostgreSQL)
> should be optional, not part of the default plan. Ask me whether I
> want to retain SQLite or migrate to another database. - Do not modify
> code or create a branch yet. - Present the plan for approval.

**Checkpoint:** Bob has presented the plan and you have answered its questions. The Epic and Stories appear as Issues in your repository. Approve the plan before continuing.

### Stage 2: Agent Mode

After approving the plan, switch Bob to **Agent Mode** and submit:

> Implement the approved modernization plan, one GitHub Story at a time.
>
> -   Create the approved feature branch in my GitHub repository.
> -   Keep all new code under `modern/`.
> -   Never modify the existing legacy application or original SQLite
>     database.
> -   Implement the React frontend and Spring Boot backend.
> -   Use the database approach approved in the plan. By default,
>     continue using the existing SQLite database.
> -   Implement the required banking functionality and preserve existing
>     business rules.
> -   Give the dashboard a clean, modern and visually appealing look
>     suitable for a workshop demo, without spending excessive time on
>     UI customization.
> -   Write and run tests for each Story.
> -   Commit and push changes to my GitHub repository, and update the
>     corresponding GitHub Issue after completing each Story.
>
> **Important:** - Use my GitHub repository configured as `origin` for
> all branches, commits, pushes and Issue updates. Do not push changes
> to the facilitator's public repository. - Ask me whenever multiple
> valid implementation approaches, libraries, architectural decisions or
> trade-offs exist. Briefly explain the options, recommend one and wait
> for my decision. - If database migration was not selected during
> planning, do not introduce it automatically. - Do not skip tests or
> mark incomplete Stories as complete.

**Checkpoint:** Your repository has the feature branch, the `modern/` folder, tests and pushed commits, and the Issues reflect progress. Implementation may finish Story by Story, so review the remaining work before moving on.

### Stage 3: Final Validation

Once implementation is complete, submit:

> Perform end-to-end validation of the modernized banking application.
>
> -   Verify the banking workflows and business rules.
> -   Check that the dashboard and key screens render correctly and are
>     presentable for the demo.
> -   Validate database operations and data integrity.
> -   Run automated tests and verify Docker deployment.
> -   Confirm that the original legacy application and SQLite database
>     remain unchanged.
> -   Resolve failures and update the relevant GitHub Issues.
> -   Push completed changes to my GitHub repository and create a Pull
>     Request to its `main` branch.
>
> Ask me before making significant changes outside the approved scope or
> merging the Pull Request.
>
> Provide a summary of implemented features, test results, deployment
> instructions and the Pull Request URL.

**Checkpoint:** You have the test results, validation summary and Pull Request URL. Do not merge the Pull Request unless the facilitator asks you to.

---

## 5. Run the modernized app locally

Use the project README in `modern/`, if Bob created one. Its commands take priority over the ones below.

**Backend** (from the backend folder):

```bash
mvn spring-boot:run
```

**Frontend** (from the frontend folder, in a second terminal):

```bash
npm install
npm run dev
```

Open the URL shown in the terminal, usually `http://localhost:5173`.

**Or run both with Docker Compose** (if configured):

```bash
docker compose up --build
```

**Run the tests:** `mvn test` in the backend folder and `npm test` in the frontend folder (if the project defines it).

### Demo checklist

- [ ] Dashboard opens and looks presentable
- [ ] Backend starts and connects to the database
- [ ] Login works with the documented demo credentials
- [ ] Customer lookup, deposit, withdrawal and fund transfer work
- [ ] Transaction history is shown

Use only synthetic demo data. Never use real customer data or production credentials.

---

## 6. Troubleshooting and guardrails

| Problem | Fix |
|---------|-----|
| `java`, `mvn`, `node` or `git` not found | Install the tool and add it to `PATH` (set `JAVA_HOME` for Java/Maven). Open a new terminal. |
| Push rejected or authentication error | Check that `git remote -v` shows your repository, then check GitHub authentication |
| GitHub Issues not created | Check Bob's GitHub connection, selected repository and permissions |
| Backend or frontend won't start, or the frontend can't reach the backend | Check Java/Node versions, port conflicts, backend URL and CORS settings |
| Database connection fails | Check the SQLite file path and permissions |

### Guardrails

- Never push to the facilitator's repository. Push all branches, commits, Issues and Pull Requests to your own.
- Keep the legacy source and `legacybank.db` unchanged. Put all new code in `modern/`.
- Don't commit build output (`target/`) or `legacybank.db`.
- Check test results and the running app. Successful code generation alone does not prove the app works.

---

## 7. Facilitator checklist

- [ ] Publish the legacy repository publicly and add its URL in Section 3, Step 2.
- [ ] The repository does not include `legacybank.db`. Participants create it in Section 3, Step 4. Commit a synthetic database only if you want to skip that step.
- [ ] Test the clone-and-own-repository flow and all three prompts end to end with a separate GitHub account.
- [ ] Confirm Bob can create Issues, branches, commits, pushes and Pull Requests in a participant's repository.
- [ ] Keep a working example of the generated app's run instructions.

**Expected outcome:** Each participant shows the Issues in their repository (Stage 1), the modern app code pushed to it (Stage 2), and a validated app running locally (Stage 3).
