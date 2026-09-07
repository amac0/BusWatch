# ABOUTME: Unit tests for add_heading_ids.py, the anchor-id step of the legal page build.
# ABOUTME: Run with: python3 -m unittest scripts/test_add_heading_ids.py

import unittest

from add_heading_ids import add_ids, slug


class SlugTests(unittest.TestCase):
    def test_matches_github_style_anchors(self):
        self.assertEqual(slug("4. Permissions"), "4-permissions")
        self.assertEqual(slug("Children's Privacy"), "childrens-privacy")
        self.assertEqual(slug("Disclaimers, Liability, and Responsibility"), "disclaimers-liability-and-responsibility")

    def test_ignores_inline_markup_and_entities(self):
        self.assertEqual(slug("Who <em>We</em> Are &amp; More"), "who-we-are-more")


class AddIdsTests(unittest.TestCase):
    def test_adds_ids_to_every_heading_level(self):
        document = "<h1>BusWatch Privacy Policy</h1>\n<h2>2. What BusWatch Does</h2>\n<h3>3.1 Your Location</h3>"
        rendered = add_ids(document)
        self.assertIn('<h1 id="buswatch-privacy-policy">', rendered)
        self.assertIn('<h2 id="2-what-buswatch-does">', rendered)
        self.assertIn('<h3 id="31-your-location">', rendered)

    def test_leaves_other_markup_alone(self):
        self.assertEqual(add_ids("<p>no headings</p>"), "<p>no headings</p>")


if __name__ == "__main__":
    unittest.main()
