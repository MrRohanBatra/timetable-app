import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:hive/hive.dart';

import 'main.dart';

class ManageTimetableScreen extends StatefulWidget {
  const ManageTimetableScreen({super.key, this.initialDay = "Monday"});
  final String initialDay;

  @override
  State<ManageTimetableScreen> createState() => _ManageTimetableScreenState();
}

class _ManageTimetableScreenState extends State<ManageTimetableScreen> {
  late Map<String, dynamic> timetable;
  late String selectedDay;

  // Fill this with your subjects
  final List<String> suggestions = [
    "Statistics",
    "Economy, Policy and Business Environment",
    "Solid State Electronic Devices",
    "Photovoltaic Techniques",
    "Medical and Industrial Applications of Nuclear Radiations",
    "Operations Research",
    "Urban Sociology",
    "Cyber Security",
    "Popular Literature",
    "Polymer Chemistry",
    "Waste to Energy Conversion",
    "Applied Mathematical Methods",
    "Applied Statistical Mechanics",
    "Applicational Aspects of Differential Equations",
    "Cosmetic Chemistry",
    "GenAI Workshop",
    "Advanced Data Structures and Algorithms Workshop",
    "Ethical Hacking Workshop",
    "Green Fuels",
    "Introduction to Creative & Professional Writing",
    "Media Literacy",
    "Basics of Artificial Intelligence",
    "Scientific Writing and Communication",
    "Biorisk and Biosecurity",
    "Organisational Communication",
    "Waste to Energy: Technologies for Circular Economy",
    "Rechargeable Battery Science and Technology",
    "International Humanitarian Law: Practice & Policy",
    "Statistical Methods for Machine Learning",
    "Advanced Java Programming Workshop",
    "Web Technology and Cyber Security",
    "Fundamentals of Soft Computing",
    "Introduction to Large Scale Database Systems",
    "Mobile Communication",
    "Machine Learning for Signal Processing",
    "Fundamentals of Electric Vehicle",
    "Nano Manufacturing",
    "Sensor Technology & Android Programming",
    "Semiconductor Devices and Circuits",
    "Bioeconomics",
    "Genetic Disorders and Personalized Medicine",
    "Open Source Software Development",
    "Fundamentals of Distributed and Cloud Computing",
    "Concepts of Graph Theory",
    "Big Data Ingestion",
    "Digital Hardware Design",
    "Control Systems",
    "RF and Microwave Engineering",
    "Introduction to FPGA Design",
    "Biopharmaceutics and Pharmacokinetics",
    "Antimicrobial Resistance",
    "Investment Management",
    "Game Theory for Engineers",
    "Effective Tools for Career Management and Development",
    "Global Politics",
    "Health Communication",
    "Marketing Management",
    "Literature and Adaption",
    "Development Issues and Rural Engineering",
    "Political Philosophy",
    "Indian Literature",
    "Computer Networks and Internet of Things",
    "Computer Networks and Internet of Things Lab",
    "Software Engineering",
    "Artificial Intelligence",
    "Software Engineering Lab",
    "Artificial Intelligence Lab",
    "Cloud Based Enterprise Systems",
    "Cloud Based Enterprise Systems Lab",
    "Data Mining and Web Algorithms",
    "Data Mining and Web Algorithms Lab",
    "Telecommunication Networks",
    "Telecommunication Networks Lab",
    "VLSI Design",
    "VLSI Design Lab",
    "Semiconductor Materials Synthesis and Characterization",
    "VLSI Verification and Testing Lab",
    "Information Coding Theory for Wireless Communications",
    "Advance Wireless Technologies",
    "Introduction to AI & ML",
    "Advance Wireless Technologies Lab",
    "Introduction to IoT and Embedded Systems",
    "IoT and Embedded Systems Lab",
    "Fundamentals of Natural Language Processing",
    "VLSI System Design Lab"
  ];

  @override
  void initState() {
    super.initState();
    timetable = loadTimetable();
    this.selectedDay = widget.initialDay;
  }

  void _save() {
    Hive.box('timetable').put('schedule_data', json.encode(timetable));
    setState(() {});
  }

  void _addNewClass() async {
    String? pickedTime = await selectTimeRange(context);
    if (pickedTime == null) return;

    final subjectController = TextEditingController();
    final roomController = TextEditingController();

    // Default selection
    String selectedType = "L";

    if (!mounted) return;
    showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        // Use StatefulBuilder to update dropdown state inside dialog
        builder: (context, setDialogState) {
          return AlertDialog(
            title: Text("Add Class: $pickedTime"),
            content: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Autocomplete<String>(
                  optionsBuilder: (val) => suggestions.where(
                      (s) => s.toLowerCase().contains(val.text.toLowerCase())),
                  onSelected: (selection) => subjectController.text = selection,
                  fieldViewBuilder: (ctx, ctrl, node, onComplete) {
                    return TextField(
                      controller: ctrl,
                      focusNode: node,
                      onChanged: (val) => subjectController.text = val,
                      decoration:
                          const InputDecoration(labelText: "Subject Name"),
                    );
                  },
                ),
                TextField(
                    controller: roomController,
                    decoration: const InputDecoration(labelText: "Room")),
                const SizedBox(height: 15),
                // Class Type Selector
                DropdownButtonFormField<String>(
                  value: selectedType,
                  decoration: const InputDecoration(
                      labelText: "Class Type", border: OutlineInputBorder()),
                  items: const [
                    DropdownMenuItem(value: "L", child: Text("Lecture (L)")),
                    DropdownMenuItem(value: "T", child: Text("Tutorial (T)")),
                    DropdownMenuItem(value: "P", child: Text("Practical (P)")),
                  ],
                  onChanged: (val) {
                    if (val != null) {
                      setDialogState(() => selectedType = val);
                    }
                  },
                ),
              ],
            ),
            actions: [
              TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text("Cancel")),
              FilledButton(
                onPressed: () {
                  setState(() {
                    timetable[selectedDay][pickedTime] = {
                      "subject_name": subjectController.text.isEmpty
                          ? "No Name"
                          : subjectController.text,
                      "classroom": roomController.text,
                      "class_type": selectedType, // Now uses the selected value
                      "teacher": "Unknown"
                    };
                  });
                  _save();
                  Navigator.pop(context);
                },
                child: const Text("Save"),
              ),
            ],
          );
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    var dayData = timetable[selectedDay] as Map;
    return Scaffold(
      appBar: AppBar(title: const Text("Manage Classes")),
      body: Column(
        children: [
          // Day Selector
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: timetable.keys
                  .map((d) => Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 4),
                        child: ChoiceChip(
                          label: Text(d),
                          selected: selectedDay == d,
                          onSelected: (val) => setState(() => selectedDay = d),
                        ),
                      ))
                  .toList(),
            ),
          ),
          Expanded(
            child: ListView.builder(
              itemCount: dayData.length,
              itemBuilder: (context, index) {
                String timeKey = dayData.keys.elementAt(index);
                return ListTile(
                  title: Text(dayData[timeKey]['subject_name']),
                  subtitle: Text(timeKey),
                  trailing: IconButton(
                    icon: const Icon(Icons.delete_sweep_rounded,
                        color: Colors.red),
                    onPressed: () {
                      setState(() => timetable[selectedDay].remove(timeKey));
                      _save();
                    },
                  ),
                );
              },
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _addNewClass,
        label: const Text("Add Class"),
        icon: const Icon(Icons.add),
      ),
    );
  }
}
