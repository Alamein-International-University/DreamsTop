# دليل الشغل مع بعض على Git (Team Contributing Guide)

أهلاً بيكم يا شباب في بروجكت **DreamsTop**!
الجايد ده معمول مخصوص عشان نشتغل مع بعض كـ Team بنظام، ومفيش كود يضيع من حد أو يحصل خناقات Conflicts بين الفروع، خصوصاً لو دي أول مرة تستخدم Git في مشروع تيم.

---

## ⛔ أهم قاعدة في البروجكت: أوعى تعمل Push على `main` علطول!

> ⚠️ **تنبيه مهم جداً**:  
> معمول **GitHub Ruleset** قافل الـ Direct Push على فرع `main` نهائي.  
> لو جربت تكتب:
> ```bash
> git push origin main
> ```
> الـ Terminal هيديك Error أحمر وهيقولك الـ branch محمي ومرفوض (`Protected branch / Remote rejected`).  
> **أي تعديل أو ميزة (Feature) هتعملها في البروجكت، لازم تعملها في Branch خاص بيك الأول، وترفع الـ branch ده، وتفتح Pull Request (PR) عشان حد من التيم يراجعه ويعمله Approve و Merge.**

---

## 🔄 يومك هيمشي إزاي؟ (الـ 8 خطوات اللي هتعملهم كل يوم)

### 1️⃣ اسحب آخر تحديث نزل على `main` قبل ما تبدأ كود
قبل ما تمد إيدك في أي كود جديد، اتأكد إنك واخد أحدث نسخة زمايلك رفعوها:
```bash
git checkout main
git pull origin main
```
*(عشان كودك يتبني على أحدث حاجة وميبقاش في فرق بينك وبين باقي التيم).*

---

### 2️⃣ افتح Branch جديد للحاجة اللي هتشتغل عليها
أوعى تكتب كود وأنت واقف على `main`! افتح فرع جديد باسم يوضح انت بتعمل ايه:
```bash
# لو بتعمل شاشة أو ميزة جديدة
git checkout -b feature/login-screen

# لو بتصلح باج أو مشكلة
git checkout -b fix/socket-disconnect

# لو بتعدل في الداتابيز أو ملفات الـ config
git checkout -b chore/database-schema
```

> 💡 **قواعد تسمية الفروع عشان شغلنا يبقى نضيف:**
> - `feature/<اسم-الميزة>`: لشاشة جديدة، زرار، سيرفيس جديدة.
> - `fix/<اسم-المشكلة>`: لتصليح باج أو إيرور.
> - `refactor/<اسم-التعديل>`: لو بتنضف الكود أو بتعيد تنظيمه بدون تغيير وظيفته.
> - `chore/<اسم-الحاجة>`: لإعدادات Maven، الداتابيز، التوثيق.

---

### 3️⃣ اكتب كودك واتأكد إنه بيعمل Compile محلياً
بعد ما تخلص الشغل وقبل ما تفكر تعمل Commit، لازم تتأكد إن الكود مفيهوش أي Syntax error والـ Maven مبوظش حاجة:
```bash
mvn clean compile
```
> ⚠️ **لو مجابلكش `BUILD SUCCESS`، صلح الإيرور الأول؛ أوعى ترفع كود مش بيعمل Compile عشان متوقفش شغل باقي التيم!**

---

### 4️⃣ اعمل Commit لكودك برسالة واضحة
شوف إيه الملفات اللي انت عدلتها:
```bash
git status
```
ضيف الملفات واكتب رسالة تفهمنا انت عملت إيه باختصار:
```bash
git add .
git commit -m "feat(client): add login view fxml and controller"
```

> 📝 **رسائل الـ Commit المنظمة اللي بنمشي بيها:**
> - `feat: ...` -> لما تضيف ميزة جديدة
> - `fix: ...` -> لما تصلح باج
> - `style: ...` -> تعديل في CSS أو تنسيق
> - `refactor: ...` -> تحسين وتنظيم كود
> - `chore: ...` -> تعديل في pom.xml أو ملفات فرعية

---

### 5️⃣ اسحب الجديد من `main` قبل ما ترفع (عشان تتفادى الـ Conflicts)
عشان تضمن إن محدش من زمايلك عمل Merge لحاجة تانية تتعارض معاك وأنت شغال:
```bash
git checkout main
git pull origin main
git checkout feature/login-screen
git merge main
```
لو مفيش تعارض، الدمج هيخلص في ثانية بدون أي مشاكل.

---

### 6️⃣ ارفع الفرع بتاعك على GitHub
ارفع فرعك لأول مرة بالأمر ده:
```bash
git push -u origin feature/login-screen
```
*(بعد كده على نفس الفرع، اكتب `git push` بس).*

---

### 7️⃣ افتح Pull Request (PR) على GitHub
1. ادخل على صفحة الريبو على GitHub.
2. هتلاقي مستطيل أصفر طلعلك فوق مكتوب فيه **"Compare & pull request"**، دوس عليه.
3. اكتب عنوان واضح للي عملته، وهتلاقي **Checklist** جاهزة (أنت شغال على client ولا server، وهل عملت `mvn clean compile`، إلخ) علم على المربعات.
4. دوس **"Create pull request"**.

---

### 8️⃣ المراجعة والـ Merge
1. ابعت لينك الـ PR على جروب التيم لأي حد يشوفه ويعمله **Approve**.
2. بعد الـ Approval بندوس **Merge pull request**.
3. احذف الفرع من GitHub عشان منسيبش فروع قديمة كتير.
4. ارجع على جهازك لفرع `main` واسحب الشغل الجديد اللي دخل:
   ```bash
   git checkout main
   git pull origin main
   ```

---

## 🆘 قسم الطوارئ (لو عكيت متقلقش، كل حاجة ليها حل)

### 🔴 الحالة 1: "نسيت وعملت commit على `main` بالغلط ومش عارف أعمل push!"
**السبب:** الـ Ruleset قفل في وشك ومنع الـ push على main.  
**الحل بدون ما تخسر ولا سطر كود كتبته:**
```bash
# 1. وأنت واقف على main، افتح فرع جديد ينقل شغلك اللي انت عملتله commit:
git branch feature/my-saved-work

# 2. رجّع فرع main زي ما هو على GitHub بالضبط:
git reset --hard origin/main

# 3. روح للفرع الجديد اللي فيه كودك محفوظ:
git checkout feature/my-saved-work

# 4. ارفع الفرع الجديد براحتك خالص:
git push -u origin feature/my-saved-work
```

---

### 🔴 الحالة 2: "كتبت كود ونسيت أعمل Branch قبل ما أبدأ خالص!"
طول ما أنت **لسة معملتش commit**، الحل سطر واحد بس:
```bash
git checkout -b feature/my-new-feature
```
الـ Git تلقائياً هياخد كل الملفات اللي انت عدلتها معاه على الفرع الجديد فوراً.

---

### 🔴 الحالة 3: "طلعلي Merge Conflict ومش فاهم العلامات الغريبة دي!"
**السبب:** أنت وزميلك عدلتم نفس السطر في نفس الملف في نفس الوقت.  
**الحل:**
1. افتح الملف المعني في الـ Editor (VS Code أو IntelliJ أو NetBeans).
2. هتلاقي علامات واضحة زي كدة:
   ```text
   <<<<<<< HEAD (كودك أنت اللي على فرعك)
   button.setText("Send Request");
   =======
   button.setText("Add Friend");
   >>>>>>> main (كود زميلك اللي جاي من main)
   ```
3. نسّق مع زميلك وشوفوا أنهي سطر الصح اللي المفروض يفضل، وامسح العلامات دي (`<<<<<<<`, `=======`, `>>>>>>>`).
4. احفظ الملف، وبعدين اكتب في التيرمينال:
   ```bash
   git add .
   git commit -m "fix: resolve merge conflicts"
   git push
   ```

---

### 🔴 الحالة 4: "ملفات فولدر `target/` أو `.class` ظهرت عندي في الـ Git!"
مجلد `target/` ده مجلد ملفات البناء وممنوع يترفع خالص على GitHub.  
هو محطوط في `.gitignore`، بس لو ظهرلك بالغلط اكتب:
```bash
git rm -r --cached **/target
git commit -m "chore: remove untracked target build artifacts"
```

---

## ⚡ ملخص الأوامر السريعة (Cheat Sheet)

| الأمر | بيعمل إيه؟ |
| :--- | :--- |
| `git status` | بيعرفك انت واقف على انهي فرع وايه الملفات اللي متعدلة |
| `git pull origin main` | بيسحب أحدث كود نزل على main من زمايلك |
| `git checkout -b <name>` | بيعمل فرع جديد وينقلك عليه في ثانية |
| `git checkout <name>` | بينقلك لفرع موجود أصلاً |
| `git add .` | بيجهز كل التعديلات اللي عملتها للـ commit |
| `git commit -m "رسالة"` | بيحفظ نقطة التعديل محلياً على جهازك |
| `git push -u origin <name>` | بيرفع فرعك لـ GitHub لأول مرة |
| `mvn clean compile` | بيتأكد إن المشروع كله سليم وبيعمل Build بدون أخطاء |
