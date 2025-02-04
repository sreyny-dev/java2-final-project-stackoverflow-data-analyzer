# [CS209A-24Fall] Final Project

## Background
In the process of software development, many questions arise. Developers often resort to Q&A websites like Stack Overflow to seek answers. Stack Overflow, a part of the Stack Exchange Network, allows users to ask and answer questions, vote on content, and earn reputation points and badges. Higher reputation unlocks additional privileges such as voting, commenting, and editing.

This project involves developing a web application using **Spring Boot** to store, analyze, and visualize **Stack Overflow Q&A data related to Java programming**. The goal is to understand common questions, answers, and resolution activities associated with Java programming.

## Data Collection (10 points)
- **Objective**: Collect Stack Overflow threads tagged `java`.
- **Requirement**: Gather at least **1000 threads**.
- **Approach**:
  - Use Stack Overflow's REST API for data retrieval.
  - Persist data offline in a **database (PostgreSQL, MySQL, etc.)** or **local files**.
  - Avoid on-the-fly API requests; all analysis should be performed on collected data.

## Part I: Data Analysis (70 points)
Each question in this section requires:
- Identifying relevant data.
- Implementing backend data analysis.
- Visualizing results on the frontend.

### 1. Java Topics (10 points)
**Question**: What are the top N most frequently asked Java topics on Stack Overflow?

### 2. User Engagement (15 points)
**Question**: What are the top N topics with the most engagement from high-reputation users?
- Engagement includes edits, answers, comments, upvotes, and downvotes.

### 3. Common Mistakes (15 points)
**Question**: What are the top N errors and exceptions frequently discussed by Java developers?
- Classify errors as **fatal errors** (e.g., `OutOfMemoryError`) and **exceptions** (e.g., checked and runtime exceptions).
- Extract error-related data from thread content using techniques like **regular expressions**.

### 4. Answer Quality (30 points)
**Question**: What factors contribute to high-quality answers (accepted or highly upvoted answers)?
- Investigate:
  1. **Elapsed time** between question creation and first answer.
  2. **Reputation** of the answering user.
  3. **Propose one additional factor** affecting answer quality.
- Visualize findings effectively.

## Part II: RESTful Service (20 points)
Develop a **REST API** to answer the following questions in **JSON format**:
1. **Topic Frequency**: Retrieve the frequency of a specific topic or the top N topics sorted by frequency.
2. **Bug Frequency**: Retrieve the frequency of a specific error/exception or the top N errors/exceptions sorted by frequency.

## Implementation Requirements
- Implement data analysis using **Java Collections, Lambda, and Stream API**.
- Backend: **Spring Boot**.
- Database: **PostgreSQL/MySQL** (or local files for storage).
- API responses should be in **JSON format**.

## Notes
- **Start data collection early** due to API rate limits and potential instabilities.
- Ensure **meaningful data analysis** and **effective visualizations**.
- Utilize **frontend charts** for presenting insights.

## How to Run the Code
1. Use `docker compose up -d --build` to start the services.
2. Run the project.
3. Check the **Dockerfile configuration** for database connection details.

---

**Deadline**: 2024-11-10
