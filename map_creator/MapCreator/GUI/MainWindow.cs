﻿using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Windows.Forms;
using MapCreator.Render;
using MapCreator.Render.Sprite;
using MouseEventArgs = System.Windows.Forms.MouseEventArgs;

namespace MapCreator.GUI
{
    public partial class MainWindow : Form
    {
        private bool _loaded;

        private string _path = "new";
        private bool _mapModified;

        private readonly Game _game = new Game();

        public Game Game
        {
            get { return _game; }
        }

        private readonly System.Timers.Timer _timer = new System.Timers.Timer(50.0f);

        public MainWindow()
        {
            InitializeComponent();

            Texture.Load();

            _timer.Elapsed += (sender, args) =>
            {
                glControl.Invalidate();
            };
            _timer.Start();
        }

        private void glControl_Load(object sender, EventArgs e)
        {
            _loaded = true;

            _game.Initialize(glControl.Width, glControl.Height);
            _game.SetControls(spriteList);

            SetSize(new Size(1496, 781));
            CenterToScreen();
        }

        private void glControl_Paint(object sender, PaintEventArgs e)
        {
            if (!_loaded) { return; }

            _game.Render();

            glControl.SwapBuffers();
        }

        public void SetSize(Size size)
        {
            this.Width = size.Width;
            this.Height = size.Height;

            glControl_Resize(this, EventArgs.Empty);
        }

        private void glControl_Resize(object sender, EventArgs e)
        {
            _game.Resize(glControl.Width, glControl.Height);
        }

        private void spriteList_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (spriteList.SelectedItem == null)
            {
                return;
            }

            if (_game.Selected != null) { _game.Selected.Selected = false; }

            _game.Selected = (MapObject)spriteList.SelectedItem;
            _game.Selected.Selected = true;

            _game.Border.AdjustTo(_game.Selected);

            extraList.Items.Clear();
            foreach (var data in _game.Selected.ExtraData)
            {
                extraList.Items.Add(data);
            }  
        }

        private void btnAdd_Click(object sender, EventArgs e)
        {
            var window = new SpriteWindow();
            window.ShowDialog();

            if (window.SelectedTextureData == null) { return; }

            _mapModified = true;
            Text = "Ghost map creator - [" + _path + "*]";

            var count = _game.Map.Entities.Count(o => o.Id == window.SelectedTextureData.Id) + 1;
            var sprite = new MapObject(window.SelectedTextureData.Id, window.SelectedTextureData.Name + " " + count);
            _game.AddSprite(sprite);

            spriteList.Items.Add(sprite);
        }

        private void btnRemove_Click(object sender, EventArgs e)
        {
            _mapModified = true;
            Text = "Ghost map creator - [" + _path + "*]";

            Game.RemoveSprite();
        }

        private void glControl_MouseDoubleClick(object sender, MouseEventArgs e)
        {
            _game.HandleClick(e.X, -e.Y + (int) Game.Height);
        }

        private bool _mouseDown;
        private int _ox;
        private int _oy;

        private void glControl_MouseUp(object sender, MouseEventArgs e)
        {
            _mouseDown = false;
        }

        private Sprite.Edge _clickedEdge;
        private void glControl_MouseDown(object sender, MouseEventArgs e)
        {
            if (e.Button == MouseButtons.Right)
            {
                //Right click to deselect
                spriteList.SelectedItem = null;
                _game.Selected.Selected = false;
                _game.Selected = null;
                extraList.Items.Clear();
                return;
            }

            _mouseDown = true;
            _ox = e.X;
            _oy = -e.Y + (int) Game.Height;

            if (_game.Border != null && spriteList.SelectedItem != null)
            {
                                                                 //6px border. Change if it's a pain.
                _clickedEdge = _game.Border.EdgeLocation(_ox, _oy, 3 * Border.Thickness);
            }
        }

        private void glControl_MouseMove(object sender, MouseEventArgs e)
        {
            if (!_mouseDown || spriteList.SelectedIndex == -1) { return; }

            var sprite = (MapObject) spriteList.SelectedItem;

            //This seemed like a good idea, but turned out to be a usability problem, at least to me.
            //Feel free to uncomment if it's needed again.
            //if (!sprite.Contains(e.X, -e.Y + (int) Game.Height)) { return; }

            var dx = e.X - _ox;
            var dy = -e.Y + (int) Game.Height - _oy;

            if (dx != 0 || dy != 0)
            {
                _mapModified = true;
                Text = "Ghost map creator - [" + _path + "*]";
            }

            if (_clickedEdge.HasFlag(Sprite.Edge.Right))
            {              
                var theta = sprite.RadRotation;
                var cos = Math.Cos(theta);
                var sin = Math.Sin(theta);

                var delta = (float) (dx * cos + dy * sin);
                var px = (delta / 2.0f) * cos;
                var py = (delta / 2.0f) * sin;

                sprite.Width += delta;
                sprite.X += (float) px;
                sprite.Y += (float) py;
            }
            else if (_clickedEdge.HasFlag(Sprite.Edge.Left))
            {
                var theta = sprite.RadRotation;
                var cos = Math.Cos(theta);
                var sin = Math.Sin(theta);

                var delta = (float)(dx * cos + dy * sin);
                var px = (delta / 2.0f) * cos;
                var py = (delta / 2.0f) * sin;

                sprite.Width -= delta;
                sprite.X += (float)px;
                sprite.Y += (float)py;
            }

            if (_clickedEdge.HasFlag(Sprite.Edge.Top))
            {
                var theta = sprite.RadRotation;
                var cos = Math.Cos(theta);
                var sin = Math.Sin(theta);

                var delta = (float)(-dx * sin + dy * cos);
                var px = (delta / 2.0f) * sin;
                var py = (delta / 2.0f) * cos;

                sprite.Height += delta;
                sprite.X -= (float)px;
                sprite.Y += (float)py;
            }
            else if (_clickedEdge.HasFlag(Sprite.Edge.Bottom))
            {
                var theta = sprite.RadRotation;
                var cos = Math.Cos(theta);
                var sin = Math.Sin(theta);

                var delta = (float)(-dx * sin + dy * cos);
                var px = (delta / 2.0f) * sin;
                var py = (delta / 2.0f) * cos;

                sprite.Height -= delta;
                sprite.X -= (float)px;
                sprite.Y += (float)py;
            }

            if (_clickedEdge == Sprite.Edge.None)
            {
                sprite.X += dx;
                sprite.Y += dy;
            }

            _game.Border.AdjustTo(sprite);

            _ox = e.X;
            _oy = -e.Y + (int) Game.Height;
        }

        private void glControl_MouseWheel(object sender, MouseEventArgs e)
        {
            if (spriteList.SelectedIndex == -1) { return; }

            _mapModified = true;
            Text = "Ghost map creator - [" + _path + "*]";

            var item = ((MapObject) spriteList.SelectedItem);
            item.Rotation += 2 * Math.Sign(e.Delta);
            _game.Border.AdjustTo(item);
        }

        private void glControl_PreviewKeyDown(object sender, PreviewKeyDownEventArgs e)
        {
            if (spriteList.SelectedIndex == -1) { return; }

            _mapModified = true;
            Text = "Ghost map creator - [" + _path + "*]";

            var sprite = (MapObject) spriteList.SelectedItem;

            switch (e.KeyCode) { 
                case Keys.Up:
                    sprite.Y++;
                    break;
                case Keys.Down:
                    sprite.Y--;
                    break;
                case Keys.Left:
                    sprite.X--;
                    break;
                case Keys.Right:
                    sprite.X++;
                    break;
            }
        }

        private void saveToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (_path == "new" && saveFileDialog.ShowDialog() != DialogResult.OK)
            {
                return;
            }

            _path = saveFileDialog.FileName;
            File.WriteAllText(_path, _game.Map.Json);

            _mapModified = false;
            Text = "Ghost map creator - [" + _path + "]";
        }

        private void saveAsToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (saveFileDialog.ShowDialog() != DialogResult.OK) { return; }

            _path = saveFileDialog.FileName;
            File.WriteAllText(_path, _game.Map.Json);

            _mapModified = false;
            Text = "Ghost map creator - [" + _path + "]";
        }

        private void openMapToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (openFileDialog.ShowDialog() != DialogResult.OK) { return; }

            if (_mapModified)
            {
                var result = MessageBox.Show(this,
                    "Are you sure you want to open a new map without saving your current changes?",
                    "Sure?", MessageBoxButtons.YesNo, MessageBoxIcon.Question);

                if (result != DialogResult.Yes) { return; }
            }

            _path = openFileDialog.FileName;
            _game.Open(_path);

            _mapModified = false;
            Text = "Ghost map creator - [" + _path + "]";
        }

        private void sizeToolStripMenuItem_Click(object sender, EventArgs e)
        {
            var size = new SizeForm(this);
            size.ShowDialog();

            _mapModified = false;
            Text = "Ghost map creator - [" + _path + "]";
        }

        private void backgroundImageToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (openBackgroundDialog.ShowDialog() != DialogResult.OK) { return; }

            _mapModified = true;
            Text = "Ghost map creator - [" + _path + "*]";

            _game.Map.BackgroundTexture = Path.GetFileName(openBackgroundDialog.FileName);
            _game.BackgroundSprite = new Sprite(openBackgroundDialog.FileName);

            SetSize(new Size(_game.BackgroundSprite.Texture.Width + 216, _game.BackgroundSprite.Texture.Height + 61));
            MaximumSize = Size;
        }

        private void exitToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Application.Exit();
        }

        private void btnAddExtra_Click(object sender, EventArgs e)
        {
            if (spriteList.SelectedIndex == -1) { return; }

            var item = (MapObject) spriteList.SelectedItem;

            if (item.ExtraData.ContainsKey("extra data"))
            {
                MessageBox.Show("You have already created a new property.\nUse that one first.");
                return;
            }

            item.ExtraData.Add("extra data", "value");
            var data = new KeyValuePair<string, string>("extra data", "value");
            extraList.SelectedIndex = extraList.Items.Add(data);

            ShowEditDialog(item, data);
        }

        private void btnRemoveExtra_Click(object sender, EventArgs e)
        {
            if (spriteList.SelectedIndex == -1) { return; }

            var item = (MapObject) spriteList.SelectedItem;

            var property = (KeyValuePair<string, string>) extraList.SelectedItem;
            item.ExtraData.Remove(property.Key);
            extraList.Items.Remove(property);
        }

        private void ShowEditDialog(MapObject sprite, KeyValuePair<string, string> data)
        {
            var dialog = new ExtraDataForm(data);

            if (dialog.ShowDialog() == DialogResult.OK)
            {
                var newData = dialog.Data;

                if (sprite.ExtraData.ContainsKey(newData.Key)) //Overwrite
                {
                    sprite.ExtraData[newData.Key] = newData.Value;
                    extraList.Items[extraList.SelectedIndex] = dialog.Data;
                    return;
                }

                //Add new value
                sprite.ExtraData.Remove(data.Key);
                sprite.ExtraData[newData.Key] = newData.Value;
                extraList.Items[extraList.SelectedIndex] = dialog.Data;
            }
        }

        private void extraList_MouseDoubleClick(object sender, MouseEventArgs e)
        {
            if (spriteList.SelectedIndex == -1 || extraList.SelectedIndex == -1) { return; }

            var sprite = (MapObject) spriteList.SelectedItem;
            var data = (KeyValuePair<string, string>) extraList.SelectedItem;

            ShowEditDialog(sprite, data);
        }
    }
}
