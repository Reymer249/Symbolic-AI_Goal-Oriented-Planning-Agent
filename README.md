# Symbolic AI – Goal-Oriented Planning Agent

This project implements a **goal-oriented BDI agent** (Beliefs, Desires, Intentions) in **Java**. The agent must escape a maze by reasoning about the environment using a custom simplified first-order logic language (sfol) and applying planning and inference techniques.

It was developed as part of the course **Symbolic AI (4032KI), Leiden University, Assignment 3**.

## 🧠 Project Overview

The agent is designed to:

- **Perceive** the environment (maze layout, walls, keys, doors, exit).

- **Think** using a knowledge base encoded in **sfol**.

- **Decide** actions based on planning (iterative deepening search).

- **Act** by executing environment actions until it escapes the maze.

The project integrates **predicate logic, forward chaining inference, and planning** to build a symbolic reasoning system capable of solving a navigation task.

## ⚙️ Features

- **Custom logic language (sfol):**

  - Predicates, terms, and rules similar to Prolog.

  - Support for operators (`+`, `-`, `*`, `_`) for belief/goal/action manipulation.

  - Reserved predicates: `=`, `!=`, and `!` for negation.

- **BDI architecture:**

  - **Beliefs:** facts about the world (e.g., location, passages, keys).

  - **Desires:** goals the agent adopts (e.g., obtain a key, reach the exit).

  - **Intentions:** selected plans of action.

- **Planning with iterative deepening:**

  - Combines **depth-first search** with iterative deepening up to depth 7.

  - Generates plans (sequences of actions) to achieve goals.

- **Agent cycle:**

  1. Sense → process percepts into beliefs.

  2. Think → infer rules and adopt goals.

  3. Decide → generate plans using planning algorithm.

  4. Act → execute actions in the maze.

## 📂 Repository Structure

- `src`
  - `MyAgent.java` - Main implementation of the agent (logic, inference, planning)
  - `RunMe.java` - The entrace file (used to run the program)
  - `Agent.java` - Helper class to define an agent
  - `Plan.java` - Helper class to define a plan as a vector of predicates
  - `environment`
    - `Location.java` - Helper class to define one location in the world
    - `Maze.java` - Helper class representing simple maze world in which the agent can execute actions and receive percepts
  - `logic`
    - `KB.java` - This file defines a KB class that represents a knowledge base of logical sentences
    - `Predicate.java` - Helper class to define a logical predicate
    - `Sentence.java` - Helper class to define a logical statement with zero or more positive condition predicates
    - `Term.java` - Helper class to define a constant or variable in a logical predicate
- `data`
  - `percepts.txt` - Percept rules (translate maze perceptions into beliefs)
  - `program.txt` - Program rules (main agent reasoning and goal adoption)
  - `actions.txt` - Action rules (postconditions of actions)
  - `prison.txt` - Example maze environment
- `Answers.pdf` - Assignment report (answers to theory + explanations)
- `Asignment.pdf` - Assignment statement specifying the tasks to complete
- `README.md` - Project documentation (this file)

## 🚀 Getting Started

### Requirements

Java JDK 11 or higher

Eclipse IDE (or any Java IDE)

### Running the Agent

By default, the agent automatically makes decisions based on its BDI logic.

To manually select actions, set `Agent.HUMAN_DECISION` = true in `Agent.java`.

To debug belief updates, set `Agent.DEBUG = true` to print detailed knowledge base changes.

# 📖 Key Concepts Covered

Symbolic AI & Logic Programming

Predicate Logic (sfol)

Forward Chaining Inference

BDI Agent Architecture

Iterative Deepening Search Planning
