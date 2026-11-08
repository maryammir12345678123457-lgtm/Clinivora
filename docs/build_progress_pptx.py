"""Build Clinivora progress presentation (interactive 6-slide PPTX)."""
from pathlib import Path

from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE
from pptx.enum.text import PP_ALIGN
from pptx.util import Inches, Pt

ROOT = Path(r"D:\Maryam Project\Clinivora")
ASSETS = Path(r"C:\Users\ghyia\.cursor\projects\d-Maryam-Project-Clinivora\assets")
OUT = ROOT / "docs" / "Clinivora_Progress_Presentation.pptx"

DASHBOARD_SHOT = ASSETS / (
    "c__Users_ghyia_AppData_Roaming_Cursor_User_workspaceStorage_"
    "fe53d09156d6a2cd150cd7820594d801_images_image-fd514f95-14dd-4adf-93b6-c6dcd9eb8d7b.png"
)
ER_SHOT = ASSETS / (
    "c__Users_ghyia_AppData_Roaming_Cursor_User_workspaceStorage_"
    "fe53d09156d6a2cd150cd7820594d801_images_image-59fa1d77-0391-495a-a311-d24d249acee1.png"
)
ACCENT = ASSETS / "clinivora-slide-accent.jpg"
LOGO = ROOT / "src" / "main" / "resources" / "images" / "clinivora-logo.png"

BLUE = RGBColor(0x0B, 0x57, 0xD0)
DARK = RGBColor(0x1F, 0x1F, 0x1F)
MUTED = RGBColor(0x5F, 0x63, 0x68)
WHITE = RGBColor(0xFF, 0xFF, 0xFF)
SOFT = RGBColor(0xE8, 0xF0, 0xFE)
CARD = RGBColor(0xF8, 0xFA, 0xFC)
TEAL = RGBColor(0x00, 0x89, 0x7B)
ORANGE = RGBColor(0xE3, 0x74, 0x00)


def set_run(run, size=14, bold=False, color=DARK, font="Calibri"):
    run.font.size = Pt(size)
    run.font.bold = bold
    run.font.color.rgb = color
    run.font.name = font


def add_textbox(slide, left, top, width, height, text, size=14, bold=False, color=DARK, align=PP_ALIGN.LEFT):
    box = slide.shapes.add_textbox(left, top, width, height)
    tf = box.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.alignment = align
    run = p.add_run()
    run.text = text
    set_run(run, size=size, bold=bold, color=color)
    return box


def add_para(tf, text, size=13, bold=False, color=DARK, space_before=6):
    p = tf.add_paragraph()
    p.space_before = Pt(space_before)
    run = p.add_run()
    run.text = text
    set_run(run, size=size, bold=bold, color=color)
    return p


def fill_shape(shape, color):
    shape.fill.solid()
    shape.fill.fore_color.rgb = color
    shape.line.fill.background()


def add_nav_bar(slide, prs, current):
    """Interactive page buttons along the bottom."""
    labels = ["1 Home", "2 Topic", "3 DSAs", "4 Algorithm", "5 Progress", "6 Thanks"]
    width = prs.slide_width
    btn_w = Inches(1.45)
    gap = Inches(0.12)
    total = len(labels) * btn_w + (len(labels) - 1) * gap
    start = (width - total) // 2
    top = prs.slide_height - Inches(0.55)
    for i, label in enumerate(labels):
        left = start + i * (btn_w + gap)
        shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, btn_w, Inches(0.38))
        fill_shape(shape, BLUE if i == current else SOFT)
        shape.adjustments[0] = 0.2
        tf = shape.text_frame
        tf.word_wrap = False
        p = tf.paragraphs[0]
        p.alignment = PP_ALIGN.CENTER
        run = p.add_run()
        run.text = label
        set_run(run, size=10, bold=True, color=WHITE if i == current else BLUE)
        # hyperlink to slide
        add_slide_link(shape, i)


def add_slide_link(shape, slide_index):
    """Attach click action to jump to slide (0-based)."""
    # Relationship is set after all slides exist — store index for second pass
    shape._clinivora_link_index = slide_index  # type: ignore[attr-defined]


def apply_links(prs):
    slides = list(prs.slides)
    for slide in slides:
        for shape in slide.shapes:
            idx = getattr(shape, "_clinivora_link_index", None)
            if idx is None:
                continue
            # Use click action hyperlink to slide
            try:
                target = slides[idx]
                shape.click_action.target_slide = target
            except Exception:
                pass


def add_header_bar(slide, prs, title):
    bar = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, Inches(0.72))
    fill_shape(bar, BLUE)
    add_textbox(slide, Inches(0.4), Inches(0.18), Inches(10), Inches(0.4), title, size=22, bold=True, color=WHITE)
    add_textbox(
        slide, Inches(9.2), Inches(0.22), Inches(3.5), Inches(0.35),
        "Progress update · Clinivora", size=11, color=RGBColor(0xD3, 0xE3, 0xFD), align=PP_ALIGN.RIGHT,
    )


def card(slide, left, top, width, height, fill=CARD):
    shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
    fill_shape(shape, fill)
    shape.adjustments[0] = 0.08
    shape.line.color.rgb = RGBColor(0xDA, 0xDC, 0xE0)
    shape.line.width = Pt(1)
    return shape


def build():
    prs = Presentation()
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    blank = prs.slide_layouts[6]

    # —— Slide 1: Title ——
    s1 = prs.slides.add_slide(blank)
    bg = s1.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, prs.slide_height)
    fill_shape(bg, RGBColor(0xF0, 0xF4, 0xF9))
    if ACCENT.exists():
        s1.shapes.add_picture(str(ACCENT), Inches(7.2), Inches(1.0), width=Inches(5.6))
    left_panel = s1.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, Inches(6.8), prs.slide_height)
    fill_shape(left_panel, BLUE)
    if LOGO.exists():
        s1.shapes.add_picture(str(LOGO), Inches(0.55), Inches(0.55), width=Inches(0.7))
    add_textbox(s1, Inches(0.55), Inches(1.4), Inches(5.8), Inches(0.5), "CLINIVORA", size=36, bold=True, color=WHITE)
    add_textbox(
        s1, Inches(0.55), Inches(2.0), Inches(5.8), Inches(1.0),
        "Emergency & Clinic Queue\nManagement System", size=20, color=RGBColor(0xD3, 0xE3, 0xFD),
    )
    add_textbox(s1, Inches(0.55), Inches(3.3), Inches(5.8), Inches(0.4), "Progress Presentation", size=16, bold=True, color=WHITE)
    add_textbox(
        s1, Inches(0.55), Inches(3.8), Inches(5.8), Inches(0.5),
        "Java 21 · JavaFX FXML · SQLite · Custom DSAs", size=13, color=RGBColor(0xD3, 0xE3, 0xFD),
    )
    add_textbox(s1, Inches(0.55), Inches(4.6), Inches(5.8), Inches(0.35), "Group Leader", size=12, bold=True, color=RGBColor(0xAEC, 0xBFA, 0xFA) if False else RGBColor(0xAE, 0xCB, 0xFA))
    add_textbox(s1, Inches(0.55), Inches(4.95), Inches(5.8), Inches(0.35), "MIR MARYAM NAVEED", size=18, bold=True, color=WHITE)
    add_textbox(s1, Inches(0.55), Inches(5.45), Inches(5.8), Inches(0.3), "Team members", size=12, bold=True, color=RGBColor(0xAE, 0xCB, 0xFA))
    add_textbox(
        s1, Inches(0.55), Inches(5.8), Inches(5.8), Inches(0.9),
        "NUPUR TASHNOVA ISLAM\nSEEAM SAJIM\nAMINJON", size=14, color=WHITE,
    )
    add_nav_bar(s1, prs, 0)

    # —— Slide 2: Topic & architecture ——
    s2 = prs.slides.add_slide(blank)
    add_header_bar(s2, prs, "Topic & Architecture")
    add_textbox(
        s2, Inches(0.4), Inches(0.95), Inches(12.4), Inches(0.55),
        "Clinivora helps clinics manage ER triage, doctor assignment, routine queues, and operations reports.",
        size=15, color=MUTED,
    )
    card(s2, Inches(0.4), Inches(1.6), Inches(6.0), Inches(4.6))
    box = add_textbox(s2, Inches(0.65), Inches(1.8), Inches(5.5), Inches(0.4), "What we are building", size=16, bold=True, color=BLUE)
    tf = s2.shapes[-1].text_frame if False else box.text_frame
    # rebuild content in a fresh text box
    topics = s2.shapes.add_textbox(Inches(0.65), Inches(2.3), Inches(5.5), Inches(3.6))
    tf = topics.text_frame
    tf.word_wrap = True
    tf.paragraphs[0].clear()
    run = tf.paragraphs[0].add_run()
    run.text = "• Patient registration & profiles"
    set_run(run, size=14, color=DARK)
    for line in [
        "• Emergency Room: Needs Triage → Priority Queue → Active floor",
        "• ESI priority (P1–P5) for who is seen next",
        "• Routine queue, doctors, reports (PDF/CSV)",
        "• Themes & languages (EN / Urdu RTL / Chinese)",
        "• Roles: Admin, Reception, Triage, Doctor",
    ]:
        add_para(tf, line, size=14, space_before=10)

    card(s2, Inches(6.7), Inches(1.6), Inches(6.1), Inches(4.6), SOFT)
    add_textbox(s2, Inches(6.95), Inches(1.8), Inches(5.6), Inches(0.4), "UI = FXML  ·  Logic = Java", size=16, bold=True, color=BLUE)
    arch = s2.shapes.add_textbox(Inches(6.95), Inches(2.35), Inches(5.6), Inches(3.5))
    tf = arch.text_frame
    tf.word_wrap = True
    run = tf.paragraphs[0].add_run()
    run.text = "Professor requirement: screens in .fxml files"
    set_run(run, size=14, bold=True, color=DARK)
    for line in [
        "Controller (.java) maps to FXML via fx:controller",
        "Controller stands between UI and services",
        "",
        "FXML Views  →  Controllers  →  Services",
        "         ↘ Custom DSAs + SQLite",
        "",
        "14 FXML screens (Login, Dashboard, ER, …)",
        "Not built as pure-Java windows",
    ]:
        add_para(tf, line, size=13 if line else 8, bold=line.startswith("FXML"), space_before=6 if line else 2)
    add_nav_bar(s2, prs, 1)

    # —— Slide 3: Data structures ——
    s3 = prs.slides.add_slide(blank)
    add_header_bar(s3, prs, "Data Structures (5)")
    add_textbox(
        s3, Inches(0.4), Inches(0.9), Inches(12.4), Inches(0.4),
        "Course names · custom implementations (not java.util internals) · live in DSA Laboratory",
        size=13, color=MUTED,
    )
    structures = [
        ("ArrayList", "DynamicArray", "Lists, graph vertices", "Fast index · append O(1)*", BLUE),
        ("HashMap", "HashTable", "ESI counts, heap index", "Average O(1) lookup", TEAL),
        ("Priority Queue", "IndexedMinHeap", "ER who-is-next board", "Extract min O(log n)", ORANGE),
        ("Linked List", "DoublyLinkedList", "Patient / case timeline", "Insert at ends O(1)", RGBColor(0x5E, 0x35, 0xB1)),
        ("Stack", "ArrayStack", "Patients form Undo", "LIFO undo O(1)", RGBColor(0xB3, 0x26, 0x1E)),
    ]
    x0 = Inches(0.35)
    card_w = Inches(2.45)
    gap = Inches(0.12)
    for i, (course, impl, use, why, accent) in enumerate(structures):
        left = x0 + i * (card_w + gap)
        top = Inches(1.5)
        c = card(s3, left, top, card_w, Inches(4.7))
        stripe = s3.shapes.add_shape(MSO_SHAPE.RECTANGLE, left, top, card_w, Inches(0.18))
        fill_shape(stripe, accent)
        add_textbox(s3, left + Inches(0.12), top + Inches(0.35), card_w - Inches(0.2), Inches(0.7), course, size=15, bold=True, color=DARK)
        add_textbox(s3, left + Inches(0.12), top + Inches(1.05), card_w - Inches(0.2), Inches(0.55), impl, size=11, color=BLUE)
        add_textbox(s3, left + Inches(0.12), top + Inches(1.7), card_w - Inches(0.2), Inches(1.2), "Used for:\n" + use, size=12, color=DARK)
        add_textbox(s3, left + Inches(0.12), top + Inches(3.2), card_w - Inches(0.2), Inches(1.1), "Why:\n" + why, size=12, color=MUTED)
    add_nav_bar(s3, prs, 2)

    # —— Slide 4: Algorithm ——
    s4 = prs.slides.add_slide(blank)
    add_header_bar(s4, prs, "Algorithm — ESI Triage Ordering")
    add_textbox(
        s4, Inches(0.4), Inches(0.9), Inches(12.4), Inches(0.4),
        "Priority Queue (Indexed Min-Heap) decides who is treated next",
        size=15, bold=True, color=DARK,
    )
    card(s4, Inches(0.4), Inches(1.45), Inches(7.4), Inches(4.8))
    steps = s4.shapes.add_textbox(Inches(0.65), Inches(1.65), Inches(7.0), Inches(4.4))
    tf = steps.text_frame
    tf.word_wrap = True
    run = tf.paragraphs[0].add_run()
    run.text = "TriageService.buildPriorityQueue()"
    set_run(run, size=15, bold=True, color=BLUE)
    for line, bold in [
        ("1. Load emergency cases with status TRIAGED", False),
        ("2. For each case with ESI (P1–P5), PUSH into IndexedMinHeap", False),
        ("3. Order key: priority rank first (P1 before P5), then earlier arrival", False),
        ("4. POP until empty → ordered list for ER Priority Queue table", False),
        ("", False),
        ("Complexity", True),
        ("• push / pop ≈ O(log n)", False),
        ("• build full ordered board ≈ O(n log n)", False),
        ("", False),
        ("Why a heap? Efficient “next patient” extracts & priority updates", False),
        ("(vs re-sorting a full ArrayList every time)", False),
    ]:
        if line == "":
            add_para(tf, " ", size=6, space_before=2)
        else:
            add_para(tf, line, size=14, bold=bold, space_before=8)

    card(s4, Inches(8.05), Inches(1.45), Inches(4.8), Inches(4.8), SOFT)
    add_textbox(s4, Inches(8.3), Inches(1.7), Inches(4.3), Inches(0.4), "Demo path", size=16, bold=True, color=BLUE)
    demo = s4.shapes.add_textbox(Inches(8.3), Inches(2.25), Inches(4.3), Inches(3.6))
    tf = demo.text_frame
    tf.word_wrap = True
    run = tf.paragraphs[0].add_run()
    run.text = "In the running app:"
    set_run(run, size=13, bold=True, color=DARK)
    for line in [
        "1. Login as triage / admin",
        "2. Open Emergency Room",
        "3. Show Priority Queue panel",
        "4. Open DSA Laboratory",
        "5. Select Priority Queue (IndexedMinHeap)",
        "6. Run push / pop",
        "",
        "Bonus algorithm: Graph BFS O(V+E)",
    ]:
        add_para(tf, line, size=13, space_before=8)
    add_nav_bar(s4, prs, 3)

    # —— Slide 5: Progress + screenshots ——
    s5 = prs.slides.add_slide(blank)
    add_header_bar(s5, prs, "Progress — Live UI")
    add_textbox(
        s5, Inches(0.4), Inches(0.85), Inches(12.4), Inches(0.35),
        "Dashboard ops board · Emergency Room floor boards · themes & i18n · reports · accounts",
        size=12, color=MUTED,
    )
    if DASHBOARD_SHOT.exists():
        s5.shapes.add_picture(str(DASHBOARD_SHOT), Inches(0.35), Inches(1.3), width=Inches(6.3))
        add_textbox(s5, Inches(0.35), Inches(5.55), Inches(6.3), Inches(0.3), "Dashboard — KPIs, ESI mix, waits, pipeline", size=11, color=MUTED)
    if ER_SHOT.exists():
        s5.shapes.add_picture(str(ER_SHOT), Inches(6.85), Inches(1.3), width=Inches(6.1))
        add_textbox(s5, Inches(6.85), Inches(5.55), Inches(6.1), Inches(0.3), "Emergency Room — triage / priority / active floor", size=11, color=MUTED)
    add_nav_bar(s5, prs, 4)

    # —— Slide 6: Thanks ——
    s6 = prs.slides.add_slide(blank)
    bg = s6.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, prs.slide_height)
    fill_shape(bg, BLUE)
    add_textbox(s6, Inches(0.8), Inches(1.8), Inches(11.5), Inches(0.7), "Thank you — open to feedback", size=32, bold=True, color=WHITE, align=PP_ALIGN.CENTER)
    add_textbox(
        s6, Inches(1.5), Inches(2.7), Inches(10.3), Inches(0.6),
        "Progress presentation · professor may propose changes or modifications",
        size=16, color=RGBColor(0xD3, 0xE3, 0xFD), align=PP_ALIGN.CENTER,
    )
    add_textbox(
        s6, Inches(1.5), Inches(3.6), Inches(10.3), Inches(1.4),
        "Leader: MIR MARYAM NAVEED\n"
        "Members: NUPUR TASHNOVA ISLAM · SEEAM SAJIM · AMINJON\n\n"
        "Run:  mvn javafx:run   ·   Demo: admin / AdminDemo!2026",
        size=15, color=WHITE, align=PP_ALIGN.CENTER,
    )
    # Interactive jump buttons on thanks page (larger)
    jumps = [("Topic", 1), ("Data Structures", 2), ("Algorithm", 3), ("Screenshots", 4)]
    jw = Inches(2.4)
    total = len(jumps) * jw + (len(jumps) - 1) * Inches(0.2)
    start = (prs.slide_width - total) // 2
    for i, (label, idx) in enumerate(jumps):
        left = start + i * (jw + Inches(0.2))
        shape = s6.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, Inches(5.5), jw, Inches(0.5))
        fill_shape(shape, WHITE)
        shape.adjustments[0] = 0.25
        tf = shape.text_frame
        p = tf.paragraphs[0]
        p.alignment = PP_ALIGN.CENTER
        run = p.add_run()
        run.text = label
        set_run(run, size=13, bold=True, color=BLUE)
        add_slide_link(shape, idx)
    add_nav_bar(s6, prs, 5)

    apply_links(prs)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    prs.save(str(OUT))
    print(f"Wrote {OUT}")

    # Also copy to USB if present
    usb = Path(r"F:\Clinivora\docs")
    if usb.parent.exists():
        try:
            usb.mkdir(parents=True, exist_ok=True)
            dest = usb / OUT.name
            dest.write_bytes(OUT.read_bytes())
            print(f"Also copied to {dest}")
        except Exception as ex:
            print(f"USB copy skipped: {ex}")


if __name__ == "__main__":
    build()
