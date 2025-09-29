# FuzzForge

![FuzzForge Logo](docs/assets/fuzzforge-logo.png)

**AI-powered workflow automation and AI Agents for AppSec, Fuzzing & Offensive Security**

[![Discord](https://img.shields.io/discord/0000000000000?logo=discord&label=Discord&color=7289da)](https://discord.com/invite/acqv9FVG)
[![Website](https://img.shields.io/badge/Website-fuzzforge.ai-blue?logo=vercel)](https://fuzzforge.ai)
[![License](https://img.shields.io/badge/license-BSL%20%2B%20Apache-orange)](LICENSE)
![Version](https://img.shields.io/badge/version-0.6.0-green)

---

## 🚀 Overview

**FuzzForge** helps security researchers and engineers automate **application security** and **offensive security** workflows with the power of AI and fuzzing frameworks.

- Orchestrate static & dynamic analysis  
- Automate vulnerability research  
- Scale AppSec testing with AI agents  
- Build, share & reuse workflows across teams  

FuzzForge is **open source**, built to empower security teams, researchers, and the community.

---

## ⚡ Quickstart

Run your first workflow in **3 steps**:

```bash
# 1. Clone the repo
git clone https://github.com/fuzzinglabs/fuzzforge.git
cd fuzzforge

# 2. Build & run with Docker
docker compose up

# 3. Access the UI
open http://localhost:3000
```

👉 More installation options in the [Documentation](https://fuzzforge.ai/docs).

---

## 🔍 Example Workflow

Example: Run a workflow that audits an Android APK with AI agents:

```bash
fuzzforge run workflows/android_apk_audit.yaml
```

FuzzForge automatically orchestrates static analysis, AI-assisted reversing, and vulnerability triage.

---

## 🎥 Demos

### AI-Powered Workflow Execution
![LLM Workflow Demo](docs/static/videos/llm_workflow.gif)

*AI agents automatically analyzing code and providing security insights*

### Manual Workflow Setup
![Manual Workflow Demo](docs/static/videos/manual_workflow.gif)

*Setting up and running security workflows through the interface*

---

## ✨ Key Features

- 🤖 **AI Agents for Security** – Specialized agents for AppSec, reversing, and fuzzing  
- 🛠 **Workflow Automation** – Define & execute AppSec workflows as code  
- 📈 **Vulnerability Research at Scale** – Rediscover 1-days & find 0-days with automation  
- 🔗 **Fuzzer Integration** – AFL, Honggfuzz, AFLnet, StateAFL & more  
- 🌐 **Community Marketplace** – Share workflows, corpora, PoCs, and modules  
- 🔒 **Enterprise Ready** – Team/Corp cloud tiers for scaling offensive security  

---

## 📚 Resources

- 🌐 [Website](https://fuzzforge.ai)  
- 📖 [Documentation](https://fuzzforge.ai/docs)  
- 💬 [Community Discord](https://discord.com/invite/acqv9FVG)  
- 🎓 [FuzzingLabs Academy](https://academy.fuzzinglabs.com)  

---

## 🤝 Contributing

We welcome contributions from the community!  
Check out our [Contributing Guide](CONTRIBUTING.md) to get started.

---

## 📜 License

FuzzForge is released under the **Business Source License (BSL) 1.1**, with an automatic fallback to **Apache 2.0** after 4 years.  
See [LICENSE](LICENSE) and [LICENSE-APACHE](LICENSE-APACHE) for details.
